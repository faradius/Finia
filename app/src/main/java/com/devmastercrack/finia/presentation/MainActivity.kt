package com.devmastercrack.finia.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.devmastercrack.finia.core.theme.FiniaTheme
import com.devmastercrack.finia.presentation.finia.FiniaRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiniaTheme {
                FiniaRoot(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
