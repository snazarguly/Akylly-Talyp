package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.core.data.DemoDataRepository
import com.example.ui.navigation.MainAppNav
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val repository = remember { DemoDataRepository(applicationContext) }
      val themeMode by repository.themeMode.collectAsState()

      MyApplicationTheme(themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainAppNav(repository = repository)
        }
      }
    }
  }
}
