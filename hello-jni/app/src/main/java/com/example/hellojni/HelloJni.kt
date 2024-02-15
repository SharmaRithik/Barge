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
 */
package com.example.hellojni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hellojni.databinding.ActivityHelloJniBinding
import android.widget.Toast
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.View



class HelloJni : AppCompatActivity() {

    private var isLooping = false
    private var isGpuLooping = false

    private lateinit var binding: ActivityHelloJniBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelloJniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make the result TextView scrollable
        binding.resultTextView.movementMethod = ScrollingMovementMethod()

        binding.loopCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startLoopingMatrixMultiplication()
                binding.stopButton.visibility = View.VISIBLE
                binding.stopButton.isEnabled = true // Enable the Stop button
                binding.multiplyButton.isEnabled = false // Disable the Compute on CPU button
            } else {
                stopLoopingMatrixMultiplication()
                binding.multiplyButton.isEnabled = true // Re-enable the Compute on CPU button if Loop CPU is unchecked
            }
        }

        binding.loopGpuCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startLoopingMatrixMultiplicationOnGpu()
                binding.stopButton.visibility = View.VISIBLE
                binding.stopButton.isEnabled = true // Enable the Stop button
                binding.multiplyButtonGpu.isEnabled = false // Disable the Compute on GPU button
            } else {
                stopLoopingMatrixMultiplicationOnGpu()
                binding.multiplyButtonGpu.isEnabled = true // Re-enable the Compute on GPU button if Loop GPU is unchecked
            }
        }

        binding.stopButton.setOnClickListener {
            binding.loopCheckBox.isChecked = false
            binding.loopGpuCheckBox.isChecked = false
            stopLoopingMatrixMultiplication()
            stopLoopingMatrixMultiplicationOnGpu()
            binding.stopButton.visibility = View.VISIBLE
        }

        // Set up the button click for CPU
        binding.multiplyButton.setOnClickListener {
            performMatrixMultiplication()
            displaySavedResults()
        }

        // Set up the button click for GPU
        binding.multiplyButtonGpu.setOnClickListener {
            performMatrixMultiplicationGPU()
            displaySavedResults()
        }

        // Set up the clear results button click listener
        binding.clearResultsButton.setOnClickListener {
            clearAllResults()
        }

    }

    private fun startLoopingMatrixMultiplication() {
        isLooping = true
        Thread {
            while (isLooping) {
                runOnUiThread {
                    performBackgroundMatrixMultiplication()
                    showToast("Performing multiplication on CPU...")
                }
                // Delay for a short period to prevent UI overload
                Thread.sleep(500)
            }
        }.start()
    }

    private fun stopLoopingMatrixMultiplication() {
        isLooping = false
    }

    private fun startLoopingMatrixMultiplicationOnGpu() {
        isGpuLooping = true
        Thread {
            while (isGpuLooping) {
                // Perform GPU matrix multiplication
                runOnUiThread {
                    // Update your UI or display results here
                    // For example, showing a toast for demonstration
                    showToast("Performing multiplication on GPU...")
                }
                // Implement the actual GPU multiplication logic here
                // For demonstration, we just sleep the thread
                try {
                    Thread.sleep(1000) // Simulate time taken for GPU multiplication
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun stopLoopingMatrixMultiplicationOnGpu() {
        isGpuLooping = false
    }

    private fun performBackgroundMatrixMultiplication() {
        // Custom or default values for the matrix multiplication
        val rowsMat1 = 100
        val colsMat1 = 100
        val rowsMat2 = 100
        val colsMat2 = 100
        val threadCount = 2 // Example value

        // Call the native method or perform the computation directly
        val result = stringFromJNI(rowsMat1, colsMat1, rowsMat2, colsMat2, threadCount)

        // Optionally process the result here if needed
        // Note: Any UI updates based on this result need to be posted to the UI thread using runOnUiThread {}
    }
    override fun onDestroy() {
        super.onDestroy()
        // Make sure to stop the loop when the activity is destroyed
        stopLoopingMatrixMultiplication()
    }
    private fun saveResult(matrix1Rows: Int, matrix1Cols: Int, matrix2Rows: Int, matrix2Cols: Int, time: Long) {
        val sharedPreferences = getSharedPreferences("MatrixResults", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val existingResults = sharedPreferences.getString("results", "") ?: ""
        val resultString = "${matrix1Rows}X${matrix1Cols} by ${matrix2Rows}X${matrix2Cols} in ${time} milliseconds"
        val newResults = if (existingResults.isEmpty()) resultString else "$existingResults\n$resultString"
        editor.putString("results", newResults)
        editor.apply()
    }

    private fun saveFullResult(result: String) {
        val sharedPreferences = getSharedPreferences("MatrixResults", Context.MODE_PRIVATE)
        val existingResults = sharedPreferences.getString("fullResults", "") ?: ""
        // Prepend the new result to the existing results so it appears at the top
        val updatedResults = "$result\n${existingResults.trim()}" // Use trim() to remove any leading or trailing whitespace
        sharedPreferences.edit().putString("fullResults", updatedResults.trim()).apply() // Use trim() here to clean up any extra newline characters
    }

    // Call this method to load and display all saved results
    private fun displaySavedResults() {
        val sharedPreferences = getSharedPreferences("MatrixResults", Context.MODE_PRIVATE)
        val savedResults = sharedPreferences.getString("fullResults", "") ?: ""
        binding.resultTextView.text = savedResults // This will be empty if results are cleared
    }


    private fun clearAllResults() {
        val sharedPreferences = getSharedPreferences("MatrixResults", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("fullResults").apply() // This clears the results
        binding.resultTextView.text = "" // Clear the TextView to reflect the change
    }
    private fun performMatrixMultiplication() {
        // Retrieve input from EditText fields
        val rowsMat1 = binding.matrix1Rows.text.toString().toIntOrNull()
        val colsMat1 = binding.matrix1Cols.text.toString().toIntOrNull()
        val rowsMat2 = binding.matrix2Rows.text.toString().toIntOrNull()
        val colsMat2 = binding.matrix2Cols.text.toString().toIntOrNull()
        val threadCount = 2

        // Check if any of the inputs are null (i.e., invalid integers) and show an error if so
        if (rowsMat1 == null || colsMat1 == null || rowsMat2 == null || colsMat2 == null) {
            binding.helloTextview.text = "Please enter valid integers for all matrix dimensions."
            return
        }

        // Assuming stringFromJNI does the multiplication and returns a string
        try {
            val result = stringFromJNI(rowsMat1, colsMat1, rowsMat2, colsMat2,threadCount)
            binding.helloTextview.text = result
            if (result != null) {
                saveFullResult(result)
            } // Save the complete result
            displaySavedResults()
        } catch (e: Exception) {
            // Handle any errors, such as incorrect matrix sizes for multiplication
            binding.helloTextview.text = "Error during matrix multiplication: ${e.message}"
        }
    }

    private fun performMatrixMultiplicationGPU() {
        val rowsMat1 = binding.matrix1Rows.text.toString().toIntOrNull() ?: 0
        val colsMat1 = binding.matrix1Cols.text.toString().toIntOrNull() ?: 0
        val rowsMat2 = binding.matrix2Rows.text.toString().toIntOrNull() ?: 0
        val colsMat2 = binding.matrix2Cols.text.toString().toIntOrNull() ?: 0
        val threadCount = 2 // Example value

        // Check if any of the inputs are null (i.e., invalid integers) and show an error if so
        if (rowsMat1 == null || colsMat1 == null || rowsMat2 == null || colsMat2 == null) {
            binding.helloTextview.text = "Please enter valid integers for all matrix dimensions."
            return
        }

        // Assuming stringFromJNI does the multiplication and returns a string
        try {
            val result = performMatrixMultiplicationOnGPU(rowsMat1, colsMat1, rowsMat2, colsMat2)
            binding.helloTextview.text = result
            if (result != null) {
                saveFullResult(result)
            } // Save the complete result
            displaySavedResults()
        } catch (e: Exception) {
            // Handle any errors, such as incorrect matrix sizes for multiplication
            binding.helloTextview.text = "Error during matrix multiplication: ${e.message}"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    /*
     * A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    external fun stringFromJNI(param1: Int, param2: Int, param3: Int, param4: Int, param5: Int): String?
    external fun performMatrixMultiplicationOnGPU(param1: Int, param2: Int, param3: Int, param4: Int): String?


    /*
     * This is another native method declaration that is *not*
     * implemented by 'hello-jni'. This is simply to show that
     * you can declare as many native methods in your Java code
     * as you want, their implementation is searched in the
     * currently loaded native libraries only the first time
     * you call them.
     *
     * Trying to call this function will result in a
     * java.lang.UnsatisfiedLinkError exception !
     */

    companion object {
    /*
     * this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so
     * at the installation time by the package manager.
     */
        init {
            System.loadLibrary("hello-jni")
        }
    }
}

