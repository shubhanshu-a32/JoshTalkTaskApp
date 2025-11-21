package com.example.joshtalktaskapp.data

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class RecordingHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var startTs: Long = 0

    fun startRecording(): String {
        val dir = context.filesDir
        val file = File(dir, "rec_${System.currentTimeMillis()}.mp4")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        startTs = System.currentTimeMillis()
        return file.absolutePath
    }

    fun stopRecording(): Int {
        recorder?.apply {
            try { stop() } catch (_: Exception) {}
            release()
        }
        recorder = null
        val seconds = ((System.currentTimeMillis() - startTs) / 1000).toInt()
        return seconds
    }
}
