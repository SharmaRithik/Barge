#include <jni.h>
#include <android/log.h>
#include "easyvk.h"
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>
#include <vector>
#include <cassert>
#include <fstream>
#include <stdexcept>

// Define the shared memory size if not already defined
#if !defined(SHARED_MEMORY_SIZE)
#define SHARED_MEMORY_SIZE 4096
#endif

#if !defined(SHARED_MEMORY_SIZE_TRAVERSED)
#define SHARED_MEMORY_SIZE_TRAVERSED (SHARED_MEMORY_SIZE)
#endif

//manually convert disssasemble - spirv-
// CSR, CSC
// Function to load SPIR-V shader from the filesystem
std::vector<uint32_t> LoadSPVFromFile(const std::string& filepath) {
    // Open the file in binary mode at the end to determine the file size
    std::ifstream file(filepath, std::ios::ate | std::ios::binary);
    if (!file.is_open()) {
        throw std::runtime_error("Failed to open SPIR-V file: " + filepath);
    }

    size_t fileSize = static_cast<size_t>(file.tellg());
    // Ensure the file size is a multiple of 4 bytes (size of uint32_t)
    if (fileSize % sizeof(uint32_t) != 0) {
        throw std::runtime_error("SPIR-V file size is not a multiple of 4 bytes: " + filepath);
    }

    std::vector<uint32_t> buffer(fileSize / sizeof(uint32_t));

    // Go back to the beginning of the file and read it into the buffer
    file.seekg(0);
    file.read(reinterpret_cast<char*>(buffer.data()), fileSize);

    // Check for read failure
    if (!file) {
        throw std::runtime_error("Failed to read SPIR-V file: " + filepath);
    }

    file.close();
    return buffer;
}

std::vector<uint32_t> LoadSPIRVShader(const char* filename, AAssetManager* assetManager) {
    assert(assetManager != nullptr);
    AAsset* assetFile = AAssetManager_open(assetManager, filename, AASSET_MODE_BUFFER);
    if (!assetFile) {
        __android_log_print(ANDROID_LOG_ERROR, "VulkanShaderLoad", "Failed to open SPIR-V shader file: %s", filename);
        throw std::runtime_error("Failed to open SPIR-V shader file.");
    }

    size_t fileLength = AAsset_getLength(assetFile);
    std::vector<uint32_t> fileContent(fileLength / sizeof(uint32_t));
    AAsset_read(assetFile, fileContent.data(), fileLength);
    AAsset_close(assetFile);

    return fileContent;
}

extern "C" JNIEXPORT jstring JNICALL Java_com_example_hellojni_HelloJni_performMatrixMultiplicationOnGPU(
        JNIEnv* env,
        jobject /* this */,
        jint row1, jint col1, jint row2, jint col2) {

    // Initialize Vulkan instance without validation layers
    auto instance = easyvk::Instance(false);
    auto physicalDevices = instance.physicalDevices();
    auto device = easyvk::Device(instance, physicalDevices.at(0));

    // Specify the path to your SPIR-V shader file
    std::string spvFilePath = "/Users/rithik/AndroidStudioProjects/ndk-samples/hello-jni/app/src/main/shaders/matrix_multiply.spv";
    std::vector<uint32_t> spvCode;

    try {
        spvCode = LoadSPVFromFile(spvFilePath);
    } catch (const std::runtime_error& e) {
        __android_log_print(ANDROID_LOG_ERROR, "ShaderLoad", "%s", e.what());
        // Handle error, such as returning early or throwing a Java exception
    }

    // Assuming matrices are square for simplification
    int sizeA = row1 * col1; // Size of matrix A
    int sizeB = row2 * col2; // Size of matrix B
    int sizeC = row1 * col2; // Size of matrix C, the result matrix

    // Create Vulkan buffers for matrices A, B, and C
    auto a = easyvk::Buffer(device, sizeA * sizeof(int)); // Buffer for matrix A
    auto b = easyvk::Buffer(device, sizeB * sizeof(int)); // Buffer for matrix B
    auto c = easyvk::Buffer(device, sizeC * sizeof(int)); // Buffer for matrix C, the result

    // Fill matrices A and B with example data
    for (int i = 0; i < sizeA; ++i) {
        a.store(i, 1); // Fill matrix A with 1s for simplification
    }
    for (int i = 0; i < sizeB; ++i) {
        b.store(i, 2); // Fill matrix B with 2s for simplification
    }
    // Matrix C is implicitly filled with 0 by initialization

    std::vector<easyvk::Buffer> buffers = {a, b, c};
    auto program = easyvk::Program(device, spvCode, buffers);

    // Adjust workgroups and workgroup size based on your shader's requirements
    program.setWorkgroups(1); // Simplified example
    program.setWorkgroupSize(1); // Simplified example

    program.initialize("main"); // The entry point of the compute shader
    program.run();

    // Prepare to return the results to Java
    jintArray resultArray = env->NewIntArray(sizeC);
    if (resultArray == nullptr) {
        return nullptr; // Out of memory error thrown by JVM
    }

    jint* resultElements = env->GetIntArrayElements(resultArray, nullptr);
    for (int i = 0; i < sizeC; ++i) {
        resultElements[i] = static_cast<jint>(c.load(i));
    }

    env->ReleaseIntArrayElements(resultArray, resultElements, 0);

    // Cleanup resources
    program.teardown();
    a.teardown();
    b.teardown();
    c.teardown();
    device.teardown();
    instance.teardown();

    return reinterpret_cast<jstring>(resultArray);
}