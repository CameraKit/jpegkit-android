cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/libjpeg/include)

add_library(libjpeg STATIC IMPORTED)
set_target_properties(
    libjpeg
    PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libjpeg-turbo.a
)

include_directories(src/main/cpp/jpegkit/include)
file(GLOB SOURCES "src/main/cpp/jpegkit/*.cpp")
add_library(jpegkit SHARED ${SOURCES})

find_library(android-lib android)
find_library(gl-lib GLESv2)

target_link_libraries(
    jpegkit
    libjpeg
    ${android-lib}
    ${gl-lib}
)
