package com.example.joshtalktaskapp.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object PersistenceHelper {
    private const val FILE_NAME = "tasks.json"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun loadTasks(context: Context): MutableList<Task> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()
        val content = file.readText()
        return try {
            json.decodeFromString(content)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    fun saveTasks(context: Context, tasks: List<Task>) {
        val file = File(context.filesDir, FILE_NAME)
        val content = json.encodeToString(tasks)
        file.writeText(content)
    }
}
