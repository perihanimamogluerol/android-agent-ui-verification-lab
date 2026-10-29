package com.perimamoglu.homestockbenchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.perimamoglu.homestockbenchmark.ui.theme.HomeStockBenchmarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeStockBenchmarkTheme {
                InventoryScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
