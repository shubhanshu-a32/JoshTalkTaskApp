package com.example.joshtalktaskapp.screens

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.joshtalktaskapp.data.PersistenceHelper
import com.example.joshtalktaskapp.data.Task
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@Composable
fun TaskHistoryScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    // Load tasks from persistence
    LaunchedEffect(Unit) {
        tasks = PersistenceHelper.loadTasks(ctx)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task History") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks recorded yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        TaskHistoryItem(task = task)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Explicit, always-visible Back button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun TaskHistoryItem(task: Task) {
    val dateText = remember(task.timestamp) {
        DateFormat.getDateTimeInstance().format(Date(task.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            // Task type & time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (task.taskType) {
                        "noise_test" -> "Noise Test"
                        "text_reading" -> "Text Reading"
                        "image_description" -> "Image Description"
                        "photo_capture" -> "Photo Capture"
                        else -> task.taskType
                    },
                    fontSize = 16.sp
                )
                Text(
                    text = dateText,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text / description
            if (!task.text.isNullOrBlank()) {
                Text(
                    text = task.text,
                    fontSize = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Image if available (for image/photo tasks)
            if (!task.imagePath.isNullOrBlank()) {
                AsyncImage(
                    model = task.imagePath,
                    contentDescription = "Task image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // AUDIO PLAYER – only when there is audioPath
            if (!task.audioPath.isNullOrBlank()) {
                AudioPlayerBox(
                    audioPath = task.audioPath!!,
                    recordedDurationSec = task.durationSec ?: 0
                )
            }
        }
    }
}

@Composable
private fun AudioPlayerBox(
    audioPath: String,
    recordedDurationSec: Int
) {
    var isPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var durationMs by remember { mutableStateOf(0) }
    var currentMs by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var internalSeek by remember { mutableStateOf(false) }

    // Create / remember MediaPlayer for this audioPath
    val mediaPlayer = remember(audioPath) {
        MediaPlayer().apply {
            setDataSource(audioPath)
            setOnPreparedListener {
                durationMs = it.duration
                isPrepared = true
            }
            setOnCompletionListener {
                isPlaying = false
                currentMs = durationMs
                progress = 1f
            }
            prepareAsync()
        }
    }

    // Release when composable leaves composition
    DisposableEffect(audioPath) {
        onDispose {
            try {
                mediaPlayer.stop()
            } catch (_: Exception) { }
            mediaPlayer.release()
        }
    }

    // Update progress while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!internalSeek) {
                try {
                    currentMs = mediaPlayer.currentPosition
                    if (durationMs <= 0) durationMs = mediaPlayer.duration
                    progress = if (durationMs > 0) {
                        currentMs.toFloat() / durationMs.toFloat()
                    } else {
                        0f
                    }
                } catch (_: Exception) {
                }
            }
            delay(200L)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Recording",
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    if (!isPrepared) return@IconButton

                    if (!isPlaying) {
                        // If at end, restart
                        if (currentMs >= durationMs && durationMs > 0) {
                            mediaPlayer.seekTo(0)
                            currentMs = 0
                            progress = 0f
                        }
                        try {
                            mediaPlayer.start()
                            isPlaying = true
                        } catch (_: Exception) { }
                    } else {
                        try {
                            mediaPlayer.pause()
                        } catch (_: Exception) { }
                        isPlaying = false
                    }
                }
            ) {
                // Simple text icons instead of Material Icons (no extra dependency)
                Text(
                    text = if (isPlaying) "⏸" else "▶",
                    fontSize = 18.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = { value ->
                        internalSeek = true
                        progress = value
                        if (durationMs > 0) {
                            val newPos = (durationMs * value).toInt()
                            try {
                                mediaPlayer.seekTo(newPos)
                                currentMs = newPos
                            } catch (_: Exception) { }
                        }
                    },
                    onValueChangeFinished = {
                        internalSeek = false
                    }
                )

                val totalDisplayMs = if (durationMs > 0) durationMs else recordedDurationSec * 1000
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentMs),
                        fontSize = 11.sp
                    )
                    Text(
                        text = formatTime(totalDisplayMs),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}
