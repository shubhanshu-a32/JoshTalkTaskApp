package com.example.joshtalktaskapp.data

import android.media.MediaPlayer

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun play(path: String) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            try { stop() } catch (_: Exception) {}
            release()
        }
        mediaPlayer = null
    }
}
