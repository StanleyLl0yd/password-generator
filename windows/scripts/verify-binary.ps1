param(
    [Parameter(Mandatory = $true)]
    [string]$Executable
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $Executable)) {
    throw "Executable not found: $Executable"
}

$mt = Get-ChildItem "${env:ProgramFiles(x86)}\Windows Kits\10\bin" -Filter mt.exe -Recurse |
    Where-Object { $_.FullName -match '\\x64\\mt\.exe$' } |
    Sort-Object FullName -Descending |
    Select-Object -First 1

if (-not $mt) {
    throw "mt.exe was not found in the Windows SDK."
}

$manifest = Join-Path $env:RUNNER_TEMP "password-generator-embedded.manifest"
Remove-Item $manifest -Force -ErrorAction SilentlyContinue

& $mt.FullName "-inputresource:$Executable;#1" "-out:$manifest"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $manifest)) {
    throw "Could not extract embedded manifest."
}

[xml]$xml = Get-Content $manifest -Raw

$manager = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
$manager.AddNamespace("asmv1", "urn:schemas-microsoft-com:asm.v1")

$identity = $xml.SelectSingleNode(
    "//asmv1:dependency/asmv1:dependentAssembly/asmv1:assemblyIdentity[@name='Microsoft.Windows.Common-Controls']",
    $manager
)

if (-not $identity) {
    throw "Embedded manifest does not require Microsoft.Windows.Common-Controls."
}

if ($identity.version -ne "6.0.0.0") {
    throw "Common Controls manifest version must be 6.0.0.0, got '$($identity.version)'."
}

if ($identity.publicKeyToken -ne "6595b64144ccf1df") {
    throw "Unexpected Common Controls publicKeyToken '$($identity.publicKeyToken)'."
}

Write-Host "Embedded manifest requires Microsoft.Windows.Common-Controls 6.0.0.0."

# Catch loader/linker failures such as STATUS_ORDINAL_NOT_FOUND before shipping.
$process = Start-Process -FilePath $Executable -PassThru
try {
    Start-Sleep -Seconds 3
    $process.Refresh()

    if ($process.HasExited) {
        throw "Windows GUI process exited during launch smoke test with code $($process.ExitCode)."
    }

    if ($process.MainWindowHandle -eq 0) {
        Start-Sleep -Seconds 2
        $process.Refresh()
    }

    if ($process.HasExited) {
        throw "Windows GUI process exited during launch smoke test with code $($process.ExitCode)."
    }

    Write-Host "Windows launch smoke test passed."
}
finally {
    if (-not $process.HasExited) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
}
