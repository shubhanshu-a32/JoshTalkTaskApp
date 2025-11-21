package com.example.joshtalktaskapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskSelectionScreen(
    onTextReading: () -> Unit,
    onImageDescription: () -> Unit,
    onPhotoCapture: () -> Unit,
    onHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose a task")
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onTextReading, modifier = Modifier.fillMaxWidth()) {
            Text("Text Reading Task")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onImageDescription, modifier = Modifier.fillMaxWidth()) {
            Text("Image Description Task")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onPhotoCapture, modifier = Modifier.fillMaxWidth()) {
            Text("Photo Capture Task")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
            Text("View Task History")
        }
    }
}
