// cpu_MatrixMultiplication.h
#ifndef CPU_MATRIX_MULTIPLICATION_H
#define CPU_MATRIX_MULTIPLICATION_H

#include <jni.h>
#include <vector>
#include <string>

// JNI function declarations
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jobjectArray JNICALL Java_com_example_barge_MainActivity_runMatrixMultiplication(
        JNIEnv* env,
        jobject thiz,
        jobjectArray matrix1,
        jobjectArray matrix2,
        jint numThreads);

#ifdef __cplusplus
}
#endif

// Regular C++ function declarations
std::vector<std::vector<float>> readMatrixFromFile(const std::string& filename);
void multiplyMatrices(const std::vector<std::vector<float>>& mat1, const std::vector<std::vector<float>>& mat2, std::vector<std::vector<float>>& result, int numThreads);

#endif // CPU_MATRIX_MULTIPLICATION_H
