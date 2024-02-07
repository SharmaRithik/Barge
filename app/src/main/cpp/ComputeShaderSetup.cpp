#include <easyvk.h>
#include <iostream>
#include <vector>
#include <cassert>

const int A_ROWS = 100;
const int A_COLS = 100; // Also B_ROWS in matrix multiplication context
const int B_COLS = 200;
const int size = A_ROWS * B_COLS;

int main() {
    // Initialize Vulkan instance with validation layers enabled as per your example
    auto instance = easyvk::Instance(true);
    // Get list of available physical devices
    auto physicalDevices = instance.physicalDevices();
    // Create device from the first physical device
    auto device = easyvk::Device(instance, physicalDevices.at(0));
    std::cout << "Using device: " << device.properties.deviceName << "\n";

    auto matrixA = easyvk::Buffer(device, A_ROWS * A_COLS * sizeof(float));
    auto matrixB = easyvk::Buffer(device, A_COLS * B_COLS * sizeof(float));
    auto matrixC = easyvk::Buffer(device, A_ROWS * B_COLS * sizeof(float));

    // Initialize matrices A and B with example values and clear matrix C
    // In a real scenario, you would populate A and B with actual data
    for (int i = 0; i < A_ROWS * A_COLS; i++) {
        matrixA.store(i, 1.0f); // Example data
    }
    for (int i = 0; i < A_COLS * B_COLS; i++) {
        matrixB.store(i, 2.0f); // Example data
    }
    matrixC.clear();

    // Assuming the shader file is at "path/to/matrix.spv"
    const char* shaderPath = "path/to/matrix.spv";
    std::vector<easyvk::Buffer> buffers = {matrixA, matrixB, matrixC};

    // Load the shader and create a program
    auto program = easyvk::Program(device, shaderPath, buffers);

    // Set workgroups and workgroup size (assuming 1 for simplicity)
    program.setWorkgroups(size);
    program.setWorkgroupSize(1);

    // Run the kernel
    program.initialize("main"); // Entry point, assuming "main"
    program.run();

    // Optional: Verify the result
    // Example validation code could go here

    // Cleanup
    program.teardown();
    matrixA.teardown();
    matrixB.teardown();
    matrixC.teardown();

    device.teardown();
    instance.teardown();

    return 0;
}

