#include "easyvk.h"
#include <string>
#include <vector>

int main() {
    try {
        easyvk::Instance instance;
        // Adjusted to a hypothetical method for picking a physical device
        easyvk::PhysicalDevice physicalDevice = instance.pickPhysicalDevice(); // Hypothetical method
        // Assuming a logical device is created from a physical device
        easyvk::Device device = physicalDevice.createLogicalDevice(); // Another hypothetical method

        std::string shaderPath = "path/to/matrix.spv";

        // Assuming easyvk provides a simplified way to create shader modules
        easyvk::ShaderModule shaderModule = device.createShaderModule(shaderPath);

        // And a simplified way to create and manage compute pipelines
        easyvk::Pipeline pipeline = device.createComputePipeline(shaderModule);

        // Dispatch the compute operation
        pipeline.dispatch();

        // Cleanup is assumed to be handled via destructors or a similar mechanism in a well-designed wrapper
    } catch (const std::exception& e) {
        std::cerr << "An exception occurred: " << e.what() << std::endl;
        // Handle any exceptions or errors that might occur during initialization, shader module creation, or dispatch
        return -1;
    }

    return 0;
}

