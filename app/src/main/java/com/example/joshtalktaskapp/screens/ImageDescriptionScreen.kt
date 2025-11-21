package com.example.joshtalktaskapp.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.joshtalktaskapp.data.DummyJsonRepository
import com.example.joshtalktaskapp.data.PersistenceHelper
import com.example.joshtalktaskapp.data.RecordingHelper
import com.example.joshtalktaskapp.data.Task
import java.util.UUID

@Composable
fun ImageDescriptionScreen(onDone: () -> Unit, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // Ask mic permission once
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    // UI state
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var productTitle by remember { mutableStateOf("") }
    var productDesc by remember { mutableStateOf("") }

    // Fetch product + request mic permission
    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

        try {
            val product = DummyJsonRepository.fetchRandomProduct()
            if (product != null) {
                imageUrl = product.images.firstOrNull() ?: product.thumbnail
                productTitle = product.title
                productDesc = product.description
                error = null
            } else {
                imageUrl = null
                productTitle = "Fallback product"
                productDesc = "Imagine any object in your room and describe what you see."
                error = "Could not load product from dummyjson; using fallback instructions."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            imageUrl = null
            productTitle = "Fallback product"
            productDesc = "Imagine any object in your room and describe what you see."
            error = "Error while calling dummyjson."
        } finally {
            loading = false
        }
    }

    // Recording state
    var status by remember { mutableStateOf("Hold the mic button to record for 10–20s.") }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf(0) }
    val recorder = remember { RecordingHelper(ctx) }

    // Self-checks
    var checkNoise by remember { mutableStateOf(false) }
    var checkDescription by remember { mutableStateOf(false) }
    var checkNoMistake by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Image Description Task", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Look at the image and describe what you see in detail.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> {
                Text("Loading image from dummyjson...")
            }
            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Dummyjson product image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                // Debug: show URL so you can verify it
                Text(
                    text = "Image URL: $imageUrl",
                    fontSize = 10.sp
                )
            }
            else -> {
                Text("No image available (check internet).")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Product: $productTitle", fontSize = 16.sp)
        Text("Source: dummyjson.com/products/random", fontSize = 12.sp)
        Text(productDesc, fontSize = 12.sp)

        error?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Note: $it", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MIC BUTTON AREA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val micGranted =
                            ctx.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (!micGranted) {
                            status = "Microphone permission required. Please allow it."
                            return@detectTapGestures
                        }

                        status = "Recording... keep holding (10–20s)."
                        val path = recorder.startRecording()
                        recordingPath = path
                        try {
                            awaitRelease()
                        } finally {
                            val dur = recorder.stopRecording()
                            duration = dur
                            status = when {
                                dur < 10 -> "Too short (${dur}s). Hold 10–20s."
                                dur > 20 -> "Too long (${dur}s). Hold 10–20s."
                                else -> "Recorded $dur s! Tick the checks and submit."
                            }
                        }
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎙️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checkNoise, { checkNoise = it })
            Text("No background noise")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checkDescription, { checkDescription = it })
            Text("I clearly described the main details")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checkNoMistake, { checkNoMistake = it })
            Text("No mistakes / repeats")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = {
                if (recordingPath == null || duration !in 10..20) {
                    status = "Recording invalid. Make sure you hold 10–20 seconds."
                    return@Button
                }
                if (!(checkNoise && checkDescription && checkNoMistake)) {
                    status = "Please tick all checkboxes."
                    return@Button
                }
                saveImageTask(ctx, imageUrl, productTitle, productDesc, recordingPath!!, duration)
                onDone()
            }) {
                Text("Submit")
            }
        }
    }
}

private fun saveImageTask(
    context: Context,
    imageUrl: String?,
    productTitle: String,
    productDesc: String,
    audioPath: String,
    duration: Int
) {
    val tasks = PersistenceHelper.loadTasks(context)
    val t = Task(
        id = UUID.randomUUID().toString(),
        taskType = "image_description",
        text = "$productTitle\n$productDesc",
        imagePath = imageUrl,
        audioPath = audioPath,
        durationSec = duration,
        timestamp = System.currentTimeMillis()
    )
    tasks.add(t)
    PersistenceHelper.saveTasks(context, tasks)
}
