package com.example.barge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barge.ui.theme.BargeTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {

    companion object {
        init {
            System.loadLibrary("cpu_MatrixMultiplication")
        }
        external fun multiplyMatricesJNI(row1: Int, col1: Int, row2: Int, col2: Int, numThreads: Int): Array<FloatArray>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BargeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mainScreen") {
                    composable("mainScreen") { Greeting(navController) }
                    composable("displayScreen/{matrixData}") { backStackEntry ->
                        DisplayScreen(navController, backStackEntry.arguments?.getString("matrixData") ?: "")
                    }
                    composable("resultsScreen") {
                        ResultsScreen(navController) // Pass navController as an argument
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(navController: NavController) {
    val rows1 = remember { mutableStateOf("") }
    val columns1 = remember { mutableStateOf("") }
    val rows2 = remember { mutableStateOf("") }
    val columns2 = remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Matrix Multiplication Setup",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.size(16.dp))

            Text("Input size of Matrix 1:")
            MatrixInput(rows = rows1, columns = columns1)

            Spacer(modifier = Modifier.size(32.dp))

            Text("Input size of Matrix 2:")
            MatrixInput(rows = rows2, columns = columns2)

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = {
                val matrixData = "${rows1.value},${columns1.value},${rows2.value},${columns2.value}"
                navController.navigate("displayScreen/$matrixData")
            }) {
                Text("Generate Matrices")
            }
        }
    }
}

@Composable
fun MatrixInput(rows: MutableState<String>, columns: MutableState<String>) {
    Column {
        OutlinedTextField(
            value = rows.value,
            onValueChange = { rows.value = it },
            label = { Text("Rows") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.size(8.dp))

        OutlinedTextField(
            value = columns.value,
            onValueChange = { columns.value = it },
            label = { Text("Columns") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(navController: NavController, matrixData: String) {
    val coreCount = Runtime.getRuntime().availableProcessors()
    val coreStates = remember { List(coreCount) { mutableStateOf(false) } }
    val stressCpu = remember { mutableStateOf(false) } // State for the "Stress CPU" checkbox
    val matrixSizes = matrixData.split(",").map { it.trim() }
    val numThreads = coreStates.count { it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Configuration") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text(text = "Size of the Matrices:")
            val matrixSizes = matrixData.split(",").map { it.trim() }
            if (matrixSizes.size == 4) {
                val (rows1, columns1, rows2, columns2) = matrixSizes
                Text(text = "Matrix 1: $rows1 rows, $columns1 columns")
                Text(text = "Matrix 2: $rows2 rows, $columns2 columns")
            } else {
                Text("Error: Invalid matrix data format.")
            }

            Spacer(modifier = Modifier.size(16.dp))
            Text(text = "Running on CPU cores:")

            // Display CPU core checkboxes (unchanged)
            coreStates.chunked(2).forEachIndexed { chunkIndex, chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEachIndexed { index, state ->
                        val coreIndex = chunkIndex * 2 + index // Correctly calculate the core index
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.value,
                                onCheckedChange = { newState -> state.value = newState }
                            )
                            Text(text = "${coreIndex + 1} core(s)")
                        }
                    }
                }
            }

            // Additional "Stress CPU" checkbox with automatic checking of all CPU checkboxes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = stressCpu.value,
                    onCheckedChange = { isChecked ->
                        stressCpu.value = isChecked
                        coreStates.forEach { state ->
                            state.value = isChecked // Automatically check/uncheck all CPU core checkboxes
                        }
                    }
                )
                Text(text = "Stress CPU")
            }

            Spacer(modifier = Modifier.size(16.dp))
            Text(text = "Running on GPU:")

            Button(onClick = {
                if (matrixSizes.size == 4) {
                    val rows1 = matrixSizes[0].toInt()
                    val columns1 = matrixSizes[1].toInt()
                    val rows2 = matrixSizes[2].toInt()
                    val columns2 = matrixSizes[3].toInt()
                    val result: Array<FloatArray> = MainActivity.multiplyMatricesJNI(rows1.toInt(), columns1.toInt(), rows2.toInt(), columns2.toInt(), numThreads)}
                else{
                    // can't think about it rn
                }
            }) {
                Text("Compute Matrices")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Time Taken",
                    style = MaterialTheme.typography.titleMedium
                )
                // Add more UI elements here as needed to display the results
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BargeTheme {
        Greeting(rememberNavController())
    }
}