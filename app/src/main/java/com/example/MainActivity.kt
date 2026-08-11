package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: CalculatorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by remember { mutableStateOf(true) }

        Crossfade(
          targetState = showSplash,
          animationSpec = tween(durationMillis = 500),
          label = "splash_fade"
        ) { isSplash ->
          if (isSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
          } else {
            CalculatorScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}


