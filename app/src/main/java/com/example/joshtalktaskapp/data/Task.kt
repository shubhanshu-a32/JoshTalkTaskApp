package com.example.joshtalktaskapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val taskType: String,
    val text: String? = null,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val durationSec: Int? = null,
    val timestamp: Long
)
