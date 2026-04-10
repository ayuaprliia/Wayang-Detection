package com.example.wayang_detection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.wayang_detection.ui.navigation.RootNavGraph
import com.example.wayang_detection.ui.theme.BgPrimary
import com.example.wayang_detection.ui.theme.WayangDetectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WayangDetectionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgPrimary
                ) {
                    RootNavGraph()
                }
            }
        }
    }
}