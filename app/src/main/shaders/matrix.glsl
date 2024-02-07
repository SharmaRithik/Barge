#version 450

// Define the size of the matrices.
const int A_ROWS = 100;
const int A_COLS = 100; // B_ROWS
const int B_COLS = 200;

// Define buffer for matrix A.
layout(set = 0, binding = 0) buffer MatrixA {
    float elementsA[];
} A;

// Define buffer for matrix B.
layout(set = 0, binding = 1) buffer MatrixB {
    float elementsB[];
} B;

// Define buffer for matrix C (result).
layout(set = 0, binding = 2) buffer MatrixC {
    float elementsC[];
} C;

// Local size definitions (workgroup size) for the compute shader.
// Adjust these values as needed based on hardware capabilities and requirements.
layout (local_size_x = 10, local_size_y = 10, local_size_z = 1) in;

void main() {
    // Compute global indices for the current thread.
    uint globalX = gl_GlobalInvocationID.x;
    uint globalY = gl_GlobalInvocationID.y;

    // Ensure we do not go out of bounds.
    if (globalX >= B_COLS || globalY >= A_ROWS) return;

    // Perform matrix multiplication for element (globalY, globalX).
    float sum = 0.0;
    for (int k = 0; k < A_COLS; ++k) {
        sum += A.elementsA[globalY * A_COLS + k] * B.elementsB[k * B_COLS + globalX];
    }

    // Store the result.
    C.elementsC[globalY * B_COLS + globalX] = sum;
}

