cmake_minimum_required(VERSION 3.10.2)

project("imagecompressor")

# libs_DIR 文件路径的变量
set(libs_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs)

add_library(
        jpeg
        SHARED
        IMPORTED
)

# IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libjpeg.so

set_target_properties(jpeg
        PROPERTIES
        IMPORTED_LOCATION ${libs_DIR}/${ANDROID_ABI}/libjpeg.so
        )

add_library(jpeg-turbo
        SHARED
        IMPORTED)

set_target_properties(jpeg-turbo
        PROPERTIES
        IMPORTED_LOCATION ${libs_DIR}/${ANDROID_ABI}/libturbojpeg.so
        )

##官方标准配置
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")

add_library(
        imagecompressor
        SHARED
        native-lib.cpp)

#头文件
target_include_directories(imagecompressor PRIVATE
        ${libs_DIR}/${ANDROID_ABI}/include)

#include_directories(${libs_DIR}/${ANDROID_ABI}/include)

target_link_libraries(
        imagecompressor
        jpeg
        jpeg-turbo
        android
        jnigraphics
        log)