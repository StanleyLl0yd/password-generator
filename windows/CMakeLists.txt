cmake_minimum_required(VERSION 3.25)

project(PasswordGeneratorWindows VERSION 1.6.0 LANGUAGES CXX)

if(NOT WIN32)
    message(FATAL_ERROR "The Windows application must be built on Windows.")
endif()

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(password_generator_core
    src/generator.cpp
    src/settings.cpp
)
target_include_directories(password_generator_core PUBLIC src)
target_compile_definitions(password_generator_core PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
target_compile_options(password_generator_core PRIVATE /W4 /WX /permissive- /EHsc /utf-8)
target_link_libraries(password_generator_core PUBLIC bcrypt advapi32)

add_executable(PasswordGenerator WIN32
    src/main.cpp
    src/clipboard.cpp
    src/ui.cpp
    res/password-generator.rc
)
target_compile_definitions(PasswordGenerator PRIVATE UNICODE _UNICODE WIN32_LEAN_AND_MEAN NOMINMAX)
target_compile_options(PasswordGenerator PRIVATE /W4 /WX /permissive- /EHsc /utf-8)
target_link_options(PasswordGenerator PRIVATE /DYNAMICBASE /NXCOMPAT /HIGHENTROPYVA)
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
