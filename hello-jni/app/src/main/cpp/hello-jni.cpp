/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <chrono>
#include <jni.h>
#include <string>
#include <thread>
#include <vector>
#include <android/log.h>

std::string matrixToString(const std::vector<std::vector<int>>& matrix) {
    std::string result;
    for (const auto& row : matrix) {
        for (auto val : row) {
            result += std::to_string(val) + " ";
        }
        result += "\n";
    }
    return result;
}

// Function to log matrix content
void logMatrix(const std::vector<std::vector<int>>& matrix, const std::string& name) {
    std::string matrixContent = name + ":\n";
    for (const auto& row : matrix) {
        for (int val : row) {
            matrixContent += std::to_string(val) + " ";
        }
        matrixContent += "\n";
    }
    __android_log_print(ANDROID_LOG_INFO, "MatrixLog", "%s", matrixContent.c_str());
}

// Function to multiply two matrices
std::string multiplyMatrices(const std::vector<std::vector<int>>& mat1, const std::vector<std::vector<int>>& mat2, std::vector<std::vector<int>>& result) {
    if (mat1[0].size() != mat2.size()) {
        return "Invalid matrix dimensions for multiplication.";
    }

    std::string sizeMat1Str = "Matrix 1 size: " + std::to_string(mat1.size()) + "x" + std::to_string(mat1[0].size()) + ".\n";
    std::string sizeMat2Str = "Matrix 2 size: " + std::to_string(mat2.size()) + "x" + std::to_string(mat2[0].size()) + ".\n";

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 0; i < mat1.size(); ++i) {
        for (int j = 0; j < mat2[0].size(); ++j) {
            for (int k = 0; k < mat2.size(); ++k) {
                result[i][j] += mat1[i][k] * mat2[k][j];
            }
        }
    }
    auto stop = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(stop - start);

    // Get the number of CPU cores
    unsigned int numCores = std::thread::hardware_concurrency();

    std::string resultStr = "Number of CPU cores: " + std::to_string(numCores) + ".\n" +
                            sizeMat1Str + sizeMat2Str +
                            "Matrix multiplication loop took " + std::to_string(duration.count()) + " milliseconds." + "\n";

    // Log matrices after multiplication
    logMatrix(mat1, "Matrix 1");
    logMatrix(mat2, "Matrix 2");
    logMatrix(result, "Result");

    return resultStr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_hellojni_HelloJni_stringFromJNI(JNIEnv* env,
                                                 jobject /* this */,
                                                 jint param1, jint param2,
                                                 jint param3, jint param4,
                                                 jint param5) {
    if (param2 != param3) {
        return env->NewStringUTF("Invalid matrix dimensions for multiplication.");
    }

    // Create matrices
    std::vector<std::vector<int>> mat1(param1, std::vector<int>(param2));
    std::vector<std::vector<int>> mat2(param3, std::vector<int>(param4));
    std::vector<std::vector<int>> result(param1, std::vector<int>(param4, 0.0f));

    // Fill matrices with sample data
    int value = 1;
    for (auto &row : mat1) {
        std::fill(row.begin(), row.end(), value++);
    }
    for (auto &row : mat2) {
        std::fill(row.begin(), row.end(), value++);
    }

    // Multiply matrices

    std::string hello = multiplyMatrices(mat1, mat2, result);

    //std::string hello = "Matrix Multiplication Completed";
  return env->NewStringUTF(hello.c_str());
}