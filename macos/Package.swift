// swift-tools-version: 5.10

import PackageDescription

let package = Package(
    name: "PasswordGenerator",
    platforms: [
        .macOS(.v13)
    ],
    products: [
        .executable(name: "PasswordGenerator", targets: ["PasswordGenerator"])
    ],
    targets: [
        .executableTarget(
            name: "PasswordGenerator",
            path: "Sources/PasswordGenerator"
        ),
        .testTarget(
            name: "PasswordGeneratorTests",
            dependencies: ["PasswordGenerator"],
            path: "Tests/PasswordGeneratorTests"
        )
    ]
)
