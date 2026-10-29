package com.perimamoglu.homestockbenchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.perimamoglu.homestockbenchmark.ui.theme.HomeStockBenchmarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeStockBenchmarkTheme {
                InventoryScreen()
            }
        }
    }
}
