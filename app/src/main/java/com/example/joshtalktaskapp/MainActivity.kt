package com.example.joshtalktaskapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.joshtalktaskapp.screens.NavigationHost
import com.example.joshtalktaskapp.ui.theme.JoshTalkTaskAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JoshTalkTaskAppTheme {
                NavigationHost()
            }
        }
    }
}