package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.MainAppNav
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val app = application as AkyllyTalypApp
    setContent {
      val themeMode by app.repository.themeMode.collectAsState()

      MyApplicationTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainAppNav(repository = app.repository, offlineAiService = app.offlineAiService)
        }
      }
    }
  }
}
