cmake_minimum_required(VERSION 3.25)

project(PasswordGeneratorWindows VERSION 1.6.1 LANGUAGES CXX)

if(NOT WIN32)
    message(FATAL_ERROR "The Windows application must be built on Windows.")
endif()

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

if(MSVC)
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif()

add_library(password_generator_core
    src/generator.cpp
    src/settings.cpp
)
target_include_directories(password_generator_core PUBLIC src)
target_compile_definitions(password_generator_core PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
target_compile_options(password_generator_core PRIVATE /W4 /WX /permissive- /EHsc /utf-8)
target_link_libraries(password_generator_core PUBLIC bcrypt advapi32)

set(GENERATED_DIR "${CMAKE_CURRENT_BINARY_DIR}/generated")
file(MAKE_DIRECTORY "${GENERATED_DIR}")
configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/src/version.hpp.in"
    "${GENERATED_DIR}/version.hpp"
    @ONLY
)
set(GENERATED_ICON "${GENERATED_DIR}/PasswordGenerator.ico")

add_custom_command(
    OUTPUT "${GENERATED_ICON}"
    COMMAND powershell -NoProfile -ExecutionPolicy Bypass
        -File "${CMAKE_CURRENT_SOURCE_DIR}/scripts/generate-icon.ps1"
        -Source "${CMAKE_CURRENT_SOURCE_DIR}/../artwork/app-icon.png"
        -Destination "${GENERATED_ICON}"
    DEPENDS
        "${CMAKE_CURRENT_SOURCE_DIR}/../artwork/app-icon.png"
        "${CMAKE_CURRENT_SOURCE_DIR}/scripts/generate-icon.ps1"
    VERBATIM
)
add_custom_target(password_generator_icon DEPENDS "${GENERATED_ICON}")

file(TO_CMAKE_PATH "${GENERATED_ICON}" ICON_PATH)
file(TO_CMAKE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/res/password-generator.manifest" MANIFEST_PATH)
configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/res/password-generator.rc.in"
    "${GENERATED_DIR}/password-generator.rc"
    @ONLY
)

add_executable(PasswordGenerator WIN32
    src/main.cpp
    src/clipboard.cpp
    src/ui.cpp
    "${GENERATED_DIR}/password-generator.rc"
)
add_dependencies(PasswordGenerator password_generator_icon)
target_include_directories(PasswordGenerator PRIVATE res "${GENERATED_DIR}")
target_compile_definitions(
    PasswordGenerator
    PRIVATE
        UNICODE
        _UNICODE
        WIN32_LEAN_AND_MEAN
        NOMINMAX
        WINVER=0x0A00
        _WIN32_WINNT=0x0A00
)
target_compile_options(PasswordGenerator PRIVATE /W4 /WX /permissive- /EHsc /utf-8)
target_link_options(PasswordGenerator PRIVATE
    /DYNAMICBASE
    /NXCOMPAT
    /HIGHENTROPYVA
    /MANIFEST:EMBED
    "/MANIFESTINPUT:${CMAKE_CURRENT_SOURCE_DIR}/res/password-generator.manifest"
)
target_link_libraries(PasswordGenerator PRIVATE
    password_generator_core
    user32
    gdi32
    comctl32
    shell32
    uxtheme
    dwmapi
)

enable_testing()
add_executable(password_generator_tests tests/generator_tests.cpp)
target_compile_definitions(password_generator_tests PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
target_compile_options(password_generator_tests PRIVATE /W4 /WX /permissive- /EHsc /utf-8)
target_link_libraries(password_generator_tests PRIVATE password_generator_core)
add_test(NAME generator_contract COMMAND password_generator_tests)

install(TARGETS PasswordGenerator RUNTIME DESTINATION .)
