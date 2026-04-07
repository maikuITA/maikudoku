package com.maiku.maikudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.maiku.maikudoku.navigation.AppNavGraph
import com.maiku.maikudoku.ui.theme.MaikudokuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaikudokuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    AppNavGraph(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
