param(
    [Parameter(Mandatory = $true)]
    [string]$Source,
    [Parameter(Mandatory = $true)]
    [string]$Destination
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$sourceImage = [System.Drawing.Image]::FromFile($Source)
try {
    $bitmap = New-Object System.Drawing.Bitmap 256, 256
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graphics.DrawImage($sourceImage, 0, 0, 256, 256)
        }
        finally {
            $graphics.Dispose()
        }

        $pngStream = New-Object System.IO.MemoryStream
        try {
            $bitmap.Save($pngStream, [System.Drawing.Imaging.ImageFormat]::Png)
            $png = $pngStream.ToArray()
        }
        finally {
            $pngStream.Dispose()
        }
    }
    finally {
        $bitmap.Dispose()
    }
}
finally {
    $sourceImage.Dispose()
}

$directory = Split-Path -Parent $Destination
if ($directory) {
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
}

$stream = [System.IO.File]::Open($Destination, [System.IO.FileMode]::Create)
try {
    $writer = New-Object System.IO.BinaryWriter $stream
    try {
        $writer.Write([UInt16]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]1)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([Byte]0)
        $writer.Write([UInt16]1)
        $writer.Write([UInt16]32)
        $writer.Write([UInt32]$png.Length)
        $writer.Write([UInt32]22)
        $writer.Write($png)
    }
    finally {
        $writer.Dispose()
    }
}
finally {
    $stream.Dispose()
}
