package com.example.joshtalktaskapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskDoneScreen(onMoreTasks: () -> Unit, onHistory: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Task submitted successfully!")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onMoreTasks) { Text("Perform another task") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onHistory) { Text("View task history") }
    }
}
