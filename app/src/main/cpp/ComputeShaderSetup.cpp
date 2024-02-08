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
    for (int i = 0; i < A_ROWS * A_COLS; i++) {
        matrixA.store(i, 1.0f); // Example data, assuming 'store' works as intended
    }
    for (int i = 0; i < A_COLS * B_COLS; i++) {
        matrixB.store(i, 2.0f); // Example data
    }
    matrixC.clear();

    const char* shaderPath = "/home/riksharm/Barge/app/src/main/shaders/matrix.spv";
    std::vector<easyvk::Buffer> buffers = {matrixA, matrixB, matrixC};

    auto program = easyvk::Program(device, shaderPath, buffers);
    program.setWorkgroups(size);
    program.setWorkgroupSize(1);

    program.initialize("main"); // Assuming "main" is the correct entry point
    program.run();

    // Read back the results from matrixC and print them
    std::vector<float> result(A_ROWS * B_COLS);
    for (int i = 0; i < A_ROWS * B_COLS; ++i) {
        result[i] = matrixC.load(i); // Assuming a method to read back data
    }

    // Print the resulting matrix for verification
    std::cout << "Resulting Matrix C:" << std::endl;
    for (int row = 0; row < A_ROWS; ++row) {
        for (int col = 0; col < B_COLS; ++col) {
            std::cout << result[row * B_COLS + col] << " ";
        }
        std::cout << std::endl;
    }

    // Cleanup
    program.teardown();
    matrixA.teardown();
    matrixB.teardown();
    matrixC.teardown();

    device.teardown();
    instance.teardown();

    return 0;
}

