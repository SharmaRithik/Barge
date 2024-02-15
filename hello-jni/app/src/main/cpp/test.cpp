#include <iostream>
#include <vector>
#include "easyvk.h"

const std::string SHADER_PATH = "path/to/matrix_multiply.spv";

// Function to load a SPIR-V shader from a file
std::vector<uint32_t> loadSPV(const std::string& filename) {
    std::vector<uint32_t> shaderCode;
    // Assume implementation for loading SPIR-V binary file into shaderCode vector
    return shaderCode;
}

void performMatrixMultiplication(int row1, int col1, int row2, int col2) {
    // Initialize Vulkan instance and device
    auto instance = easyvk::Instance(false); // false indicates no validation layers
    auto physicalDevices = instance.physicalDevices();
    auto device = easyvk::Device(instance, physicalDevices.at(0));

    // Create buffers for matrices A, B, and C
    size_t sizeA = row1 * col1;
    size_t sizeB = row2 * col2;
    size_t sizeC = row1 * col2;
    auto matrixA = easyvk::Buffer(device, sizeA, sizeof(float));
    auto matrixB = easyvk::Buffer(device, sizeB, sizeof(float));
    auto matrixC = easyvk::Buffer(device, sizeC, sizeof(float));

    // Initialize matrices A and B with some values
    // For simplicity, we're just filling them with 1.0
    for (int i = 0; i < sizeA; ++i) matrixA.store<float>(i, 1.0f);
    for (int i = 0; i < sizeB; ++i) matrixB.store<float>(i, 1.0f);
    
    // Load the compute shader
    auto shaderCode = loadSPV(SHADER_PATH);
    std::vector<easyvk::Buffer> buffers = {matrixA, matrixB, matrixC};

    // Create and configure the compute program
    auto program = easyvk::Program(device, shaderCode, buffers);
    program.setWorkgroups((row1 * col2) / 64); // Assuming a workgroup processes a 64 elements block
    program.setWorkgroupSize(64); // This needs to match the shader's local workgroup size

    // Add dimensions as a push constant or part of a buffer if required by the shader
    // This step is skipped due to lack of detail on how `easyvk` handles push constants or uniform buffers

    // Execute the shader
    program.initialize("main"); // Assuming "main" is the entry point of the shader
    program.run();

    // Optionally, read back and verify the result from matrixC
    // This step is skipped for brevity

    // Cleanup
    program.teardown();
    matrixA.teardown();
    matrixB.teardown();
    matrixC.teardown();
    device.teardown();
    instance.teardown();
}

int main() {
    int row1 = 256, col1 = 256, row2 = 256, col2 = 256; // Example dimensions
    performMatrixMultiplication(row1, col1, row2, col2);
    return 0;
}

