package com.example.app_tentzo

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class NavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}
