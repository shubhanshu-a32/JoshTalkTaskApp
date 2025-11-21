//package com.example.joshtalktaskapp.screens
//
//import androidx.compose.material.Surface
//import androidx.compose.runtime.*
//import com.example.joshtalktaskapp.ui.screens.StartScreen
////import com.example.joshtalktaskapp.ui.screens.TaskHistoryScreen
//
//@Composable
//fun NavigationHost() {
//    var screen by remember { mutableStateOf("start") }
//
//    Surface {
//        when (screen) {
//            "start" ->
//                StartScreen(onStart = { screen = "noise" })
//
//            "noise" ->
//                NoiseTestScreen(
//                    onDone = { screen = "tasks" },
//                    onBack = { screen = "start" }
//                )
//
//            "tasks" ->
//                TaskSelectionScreen(
//                    onTextReading = { screen = "text_reading" },
//                    onImageDescription = { screen = "image_description" },
//                    onPhotoCapture = { screen = "photo_capture" },
//                    onHistory = { screen = "history" }
//                )
//
//            "text_reading" ->
//                TextReadingScreen(
//                    onDone = { screen = "task_done" },
//                    onBack = { screen = "tasks" }
//                )
//
//            "image_description" ->
//                ImageDescriptionScreen(
//                    onDone = { screen = "task_done" },
//                    onBack = { screen = "tasks" }
//                )
//
//            "photo_capture" ->
//                PhotoCaptureScreen(
//                    onDone = { screen = "task_done" },
//                    onBack = { screen = "tasks" }
//                )
//
//            "history" ->
//                TaskHistoryScreen(
//                    onBack = { screen = "tasks" }
//                )
//
//            "task_done" ->
//                TaskDoneScreen(
//                    onMoreTasks = { screen = "tasks" },
//                    onHistory = { screen = "history" }
//                )
//        }
//    }
//}

package com.example.joshtalktaskapp.screens

import androidx.compose.material.Surface
import androidx.compose.runtime.*
import com.example.joshtalktaskapp.ui.screens.StartScreen
import com.example.joshtalktaskapp.screens.TaskSelectionScreen
import com.example.joshtalktaskapp.screens.TaskDoneScreen

@Composable
fun NavigationHost() {
    var screen by remember { mutableStateOf("start") }

    Surface {
        when (screen) {
            "start" ->
                StartScreen(
                    onStart = { screen = "noise" }
                )

            "noise" ->
                // NoiseTestScreen now only takes onDone
                NoiseTestScreen(
                    onDone = { screen = "tasks" }
                )

            "tasks" ->
                TaskSelectionScreen(
                    onTextReading = { screen = "text_reading" },
                    onImageDescription = { screen = "image_description" },
                    onPhotoCapture = { screen = "photo_capture" },
                    onHistory = { screen = "history" }
                )

            "text_reading" ->
                TextReadingScreen(
                    onDone = { screen = "task_done" },
                    onBack = { screen = "tasks" }
                )

            "image_description" ->
                ImageDescriptionScreen(
                    onDone = { screen = "task_done" },
                    onBack = { screen = "tasks" }
                )

            "photo_capture" ->
                PhotoCaptureScreen(
                    onDone = { screen = "task_done" },
                    onBack = { screen = "tasks" }
                )

            "history" ->
                TaskHistoryScreen(
                    onBack = { screen = "tasks" }
                )

            "task_done" ->
                TaskDoneScreen(
                    onMoreTasks = { screen = "tasks" },
                    onHistory = { screen = "history" }
                )
        }
    }
}
