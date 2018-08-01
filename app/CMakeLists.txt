cmake_minimum_required(VERSION 3.4.1)

add_library(demo SHARED src/main/cpp/demo.cpp)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(demo ${log-lib} ${android-lib})
