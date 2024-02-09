#ifndef CPU_MATRIX_MULTIPLICATION_H
#define CPU_MATRIX_MULTIPLICATION_H

#include <jni.h>
#include <vector>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jobjectArray JNICALL Java_com_example_barge_MainActivity_multiplyMatricesJNI(
        JNIEnv* env,
        jobject thiz,
        jint row1,
        jint col1,
        jint row2,
        jint col2,
        jint numThreads);

#ifdef __cplusplus
}
#endif

std::vector<std::vector<float>> readMatrixFromFile(const std::string& filename);
void multiplyMatrices(const std::vector<std::vector<float>>& mat1, const std::vector<std::vector<float>>& mat2, std::vector<std::vector<float>>& result, int numThreads);

#endif // CPU_MATRIX_MULTIPLICATION_H
