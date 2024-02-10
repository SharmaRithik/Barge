#include <iostream>
#include <vector>
#include <chrono>
#include "cpu_MatrixMultiplication.h"

int main() {
  // Set matrix dimensions and number of threads
  int rows1 = 1024;
  int cols1 = 1024;
  int rows2 = 1024;
  int cols2 = 1024;
  int numThreads = 8;

  // Create matrices with random values
  std::vector<std::vector<float>> mat1(rows1, std::vector<float>(cols1));
  std::vector<std::vector<float>> mat2(rows2, std::vector<float>(cols2));
  for (int i = 0; i < rows1; ++i) {
    for (int j = 0; j < cols1; ++j) {
      mat1[i][j] = static_cast<float>(rand()) / RAND_MAX;
    }
  }
  for (int i = 0; i < rows2; ++i) {
    for (int j = 0; j < cols2; ++j) {
      mat2[i][j] = static_cast<float>(rand()) / RAND_MAX;
    }
  }

  // Start timer
  std::chrono::time_point<std::chrono::high_resolution_clock> start = std::chrono::high_resolution_clock::now();

  // Perform matrix multiplication
  std::vector<std::vector<float>> result(rows1, std::vector<float>(cols2));
  multiplyMatrices(mat1, mat2, result, numThreads);

  // Stop timer
  std::chrono::time_point<std::chrono::high_resolution_clock> end = std::chrono::high_resolution_clock::now();

  // Print execution time
  std::chrono::duration<double, std::nano> elapsed = end - start;
  std::cout << "Execution time: " << elapsed.count() * 1e-9 << " seconds" << std::endl;

  return 0;
}

