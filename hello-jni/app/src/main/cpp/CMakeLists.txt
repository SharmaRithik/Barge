cmake_minimum_required(VERSION 3.18.1)

project("hello-jni")

# Specify source files
add_library(hello-jni SHARED
        hello-jni.cpp
        VulkanMatrixMultiplication.cpp
        easyvk.cpp)

# Include Vulkan headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Vulkan-Headers/include)

# If Vulkan headers are structured with vulkan/vk_video subdirectories, adjust as necessary
# Note: It's likely unnecessary to include subdirectories individually if the top-level include directory is specified
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Vulkan-Headers/include/vulkan)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Vulkan-Headers/include/vk_video)

# Link with NDK libraries
target_link_libraries(hello-jni
        android
        log
        vulkan
        )
