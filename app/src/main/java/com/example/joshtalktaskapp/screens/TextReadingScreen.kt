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
import com.example.joshtalktaskapp.data.DummyJsonRepository
import com.example.joshtalktaskapp.data.PersistenceHelper
import com.example.joshtalktaskapp.data.RecordingHelper
import com.example.joshtalktaskapp.data.Task
import java.util.UUID

@Composable
fun TextReadingScreen(onDone: () -> Unit, onBack: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // Request mic permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    // dummyjson quote state
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var taskText by remember { mutableStateOf("Loading text from dummyjson...") }
    var author by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val quote = DummyJsonRepository.fetchRandomQuote()
        if (quote != null) {
            taskText = quote.quote
            author = quote.author
            error = null
        } else {
            taskText =
                "Fallback text: Please read this sentence clearly so we can check your microphone quality."
            author = ""
            error = "Could not load text from dummyjson; using fallback."
        }
        loading = false
    }

    // Recording state
    var status by remember { mutableStateOf("Hold the mic button to record for 10–20s.") }
    var recordingPath by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf(0) }
    val recorder = remember { RecordingHelper(ctx) }

    // Self-check checkboxes
    var checkNoise by remember { mutableStateOf(false) }
    var checkPronunciation by remember { mutableStateOf(false) }
    var checkNoMistake by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Text Reading Task", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Text(taskText, fontSize = 16.sp)
        if (author.isNotEmpty()) {
            Text("- $author (dummyjson.com)", fontSize = 14.sp)
        }
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

                        // START recording
                        status = "Recording... keep holding (10–20s)."
                        val path = recorder.startRecording()
                        recordingPath = path
                        try {
                            awaitRelease()
                        } finally {
                            // STOP recording
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
            Checkbox(checked = checkNoise, onCheckedChange = { checkNoise = it })
            Text("No background noise")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checkPronunciation, onCheckedChange = { checkPronunciation = it })
            Text("Pronunciation is clear")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checkNoMistake, onCheckedChange = { checkNoMistake = it })
            Text("No mistakes while reading")
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
                if (!(checkNoise && checkPronunciation && checkNoMistake)) {
                    status = "Please tick all checkboxes."
                    return@Button
                }
                saveTask(ctx, taskText, recordingPath!!, duration)
                onDone()
            }) {
                Text("Submit")
            }
        }
    }
}

private fun saveTask(context: Context, text: String, audioPath: String, duration: Int) {
    val tasks = PersistenceHelper.loadTasks(context)
    val t = Task(
        id = UUID.randomUUID().toString(),
        taskType = "text_reading",
        text = text,
        audioPath = audioPath,
        durationSec = duration,
        timestamp = java.lang.System.currentTimeMillis()
    )
    tasks.add(t)
    PersistenceHelper.saveTasks(context, tasks)
}
