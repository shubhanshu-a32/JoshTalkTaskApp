package com.example.joshtalktaskapp.screens

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.joshtalktaskapp.data.PersistenceHelper
import com.example.joshtalktaskapp.data.Task
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID
import kotlin.math.log10

/**
 * Recorder that records ambient noise and lets us measure dB
 * and save the recording as a file.
 */
class NoiseTestRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var filePath: String? = null
    private var startTimeMs: Long = 0L

    fun start(): String {
        val file = File(context.filesDir, "noise_test_${System.currentTimeMillis()}.3gp")
        filePath = file.absolutePath

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            prepare()
            start()
        }

        startTimeMs = System.currentTimeMillis()
        return filePath!!
    }

    /**
     * Rough decibel estimation based on maxAmplitude.
     */
    fun currentDb(): Double {
        val amp = recorder?.maxAmplitude ?: 0
        if (amp <= 0) return 0.0
        return 20 * log10(amp.toDouble())
    }

    /**
     * Stop recording and return duration (sec) + file path.
     */
    fun stop(): Pair<Int, String?> {
        val end = System.currentTimeMillis()
        val durationSec = ((end - startTimeMs) / 1000).toInt().coerceAtLeast(0)

        try {
            recorder?.stop()
        } catch (_: Exception) { }
        recorder?.release()
        recorder = null

        return durationSec to filePath
    }
}

@Composable
fun NoiseTestScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current

    // Ask for mic permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    LaunchedEffect(Unit) {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val noiseRecorder = remember { NoiseTestRecorder(ctx) }

    var isTesting by remember { mutableStateOf(false) }
    var avgDb by remember { mutableStateOf<Double?>(null) }
    var message by remember {
        mutableStateOf(
            "We’ll record a short ambient sound sample.\n" +
                    "If average noise < 42 dB → Good to proceed.\n" +
                    "If ≥ 42 dB → Please move to a quieter place and test again."
        )
    }
    var canProceed by remember { mutableStateOf(false) }

    var lastAudioPath by remember { mutableStateOf<String?>(null) }
    var lastDuration by remember { mutableStateOf(0) }

    // Run the actual noise sampling when isTesting = true
    LaunchedEffect(isTesting) {
        if (isTesting) {
            val samples = mutableListOf<Double>()

            val micGranted =
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
                        PermissionChecker.PERMISSION_GRANTED

            if (!micGranted) {
                message = "Microphone permission is required to test noise."
                isTesting = false
                canProceed = false
                return@LaunchedEffect
            }

            message = "Measuring ambient noise... please stay quiet."
            avgDb = null
            canProceed = false
            lastAudioPath = null
            lastDuration = 0

            // Start recording (this will also be saved as the checkpoint)
            val path = noiseRecorder.start()

            // Sample for ~5 seconds (20 * 250ms)
            repeat(20) {
                val db = noiseRecorder.currentDb()
                if (db > 0) samples.add(db)
                delay(250)
            }

            // Stop recording, get duration & file path
            val (durSec, audioPath) = noiseRecorder.stop()
            lastAudioPath = audioPath
            lastDuration = durSec

            isTesting = false

            if (samples.isEmpty()) {
                message =
                    "Could not detect any noise level. Try again, and stay silent during the test."
                avgDb = null
                canProceed = false
                lastAudioPath = null
            } else {
                val avg = samples.average()
                avgDb = avg

                if (avg < 42.0) {
                    message =
                        "Average noise: ${"%.1f".format(avg)} dB.\nGood to proceed to the tasks screen."
                    canProceed = true
                } else {
                    message =
                        "Average noise: ${"%.1f".format(avg)} dB.\nToo noisy. Please move to a quieter place and test again."
                    canProceed = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Noise Test / Testing Voice Checkpoint", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        avgDb?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Measured average: ${"%.1f".format(it)} dB")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { isTesting = true },
            enabled = !isTesting
        ) {
            Text(if (isTesting) "Measuring..." else "Start Noise Test")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Save this ambient recording as a "noise_test" task (checkpoint)
                if (canProceed && lastAudioPath != null) {
                    val tasks = PersistenceHelper.loadTasks(ctx)
                    val t = Task(
                        id = UUID.randomUUID().toString(),
                        taskType = "noise_test",
                        text = "Ambient noise test. Avg: ${"%.1f".format(avgDb ?: 0.0)} dB",
                        imagePath = null,
                        audioPath = lastAudioPath,
                        durationSec = lastDuration,
                        timestamp = System.currentTimeMillis()
                    )
                    tasks.add(t)
                    PersistenceHelper.saveTasks(ctx, tasks)
                }
                onDone()
            },
            enabled = canProceed
        ) {
            Text("Continue to Tasks")
        }
    }
}
