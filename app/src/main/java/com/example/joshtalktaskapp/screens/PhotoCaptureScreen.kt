package com.example.joshtalktaskapp.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.joshtalktaskapp.data.PersistenceHelper
import com.example.joshtalktaskapp.data.RecordingHelper
import com.example.joshtalktaskapp.data.Task
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun PhotoCaptureScreen(onDone: () -> Unit, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // CAMERA permission
    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        hasCameraPermission =
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA) ==
                    PermissionChecker.PERMISSION_GRANTED
    }

    // MIC permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            imagePath = saveBitmapToFile(ctx, bitmap)
        }
    }

    var description by remember { mutableStateOf("") }
    val recorder = remember { RecordingHelper(ctx) }
    var status by remember { mutableStateOf("Hold the mic button to record for 10–20s audio description.") }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Photo Capture Task", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                photoLauncher.launch(null)
            }
        }) {
            Text("Open Camera & Capture Photo")
        }

        Spacer(modifier = Modifier.height(8.dp))
        capturedBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Captured photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        } ?: Text("No photo captured yet.")

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Short text description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MIC button area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val micGranted =
                            ctx.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                                    PermissionChecker.PERMISSION_GRANTED
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
                                else -> "Recorded $dur s! You can submit now."
                            }
                        }
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎙️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(status)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = {
                if (imagePath == null) {
                    status = "Please capture a photo first."
                    return@Button
                }
                if (recordingPath == null || duration !in 10..20) {
                    status = "Recording invalid. Make sure you hold 10–20 seconds."
                    return@Button
                }
                savePhotoTask(ctx, imagePath!!, description, recordingPath!!, duration)
                onDone()
            }) {
                Text("Submit")
            }
        }
    }
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
    val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return file.absolutePath
}

private fun savePhotoTask(
    context: Context,
    imagePath: String,
    description: String,
    audioPath: String,
    duration: Int
) {
    val tasks = PersistenceHelper.loadTasks(context)
    val t = Task(
        id = UUID.randomUUID().toString(),
        taskType = "photo_capture",
        text = description,
        imagePath = imagePath,
        audioPath = audioPath,
        durationSec = duration,
        timestamp = java.lang.System.currentTimeMillis()
    )
    tasks.add(t)
    PersistenceHelper.saveTasks(context, tasks)
}
