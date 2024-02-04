package com.example.barge

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.barge.ui.theme.BargeTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
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
                text = "Barge",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.size(16.dp))

            Text("Input size of Matrix 1:")
            MatrixInput(rows = rows1, columns = columns1)

            Spacer(modifier = Modifier.size(32.dp))

            Text("Input size of Matrix 2:")
            MatrixInput(rows = rows2, columns = columns2)

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = {
                val matrixData = "Rows1: ${rows1.value}, Columns1: ${columns1.value}, Rows2: ${rows2.value}, Columns2: ${columns2.value}"
                navController.navigate("displayScreen/$matrixData")
            }) {
                Text("Submit")
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display Screen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                // Remove the backgroundColor and contentColor if you're using Material 3
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text(text = "Matrix Data: $matrixData")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BargeTheme {
        // Preview functionality is limited and cannot show NavController-dependent UI.
    }
}