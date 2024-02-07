#include "easyvk.h"
#include <string>
#include <vector>

int main() {
    easyvk::Instance instance;
    easyvk::Device device = instance.createDevice();
    std::string shaderPath = "path/to/matrix.spv";

    // Assuming easyvk provides a simplified way to create shader modules
    easyvk::ShaderModule shaderModule = device.createShaderModule(shaderPath);

    // And a simplified way to create and manage compute pipelines
    easyvk::Pipeline pipeline = device.createComputePipeline(shaderModule);

    // Dispatch the compute operation
    pipeline.dispatch();

    // Cleanup
    device.destroyShaderModule(shaderModule);
    // Plus other cleanup
}

