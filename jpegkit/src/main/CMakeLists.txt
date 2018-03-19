cmake_minimum_required(VERSION 3.4.1)

set(JPEG_INC_DIR ${CMAKE_SOURCE_DIR}/cpp/libjpeg/include)
set(JPEG_BUILD_DIR ${CMAKE_SOURCE_DIR}/jniLibs)

include_directories(${JPEG_INC_DIR})

add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg PROPERTIES IMPORTED_LOCATION ${JPEG_BUILD_DIR}/${ANDROID_ABI}/libjpeg-turbo.a)

add_library(jpegkit SHARED ${CMAKE_SOURCE_DIR}/cpp/JniJpeg.cpp)

target_link_libraries(jpegkit libjpeg)
