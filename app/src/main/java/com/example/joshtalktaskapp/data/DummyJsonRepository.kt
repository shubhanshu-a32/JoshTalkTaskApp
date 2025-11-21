package com.example.joshtalktaskapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json as KotlinxJson
import java.net.URL

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import kotlinx.serialization.decodeFromString

@kotlinx.serialization.Serializable
data class Quote(
    val id: Int,
    val quote: String,
    val author: String
)

@kotlinx.serialization.Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnail: String,
    val images: List<String> = emptyList()
)

object DummyJsonRepository {

    private val json =  KotlinxJson  {
        ignoreUnknownKeys = true
    }

    /**
     * For TEXT READING task – fetch random quote from dummyjson.
     * GET https://dummyjson.com/quotes/random
     */
    suspend fun fetchRandomQuote(): Quote? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://dummyjson.com/quotes/random")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            conn.inputStream.use { input ->
                val body = input.bufferedReader().readText()
                json.decodeFromString(Quote.serializer(), body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * For IMAGE DESCRIPTION task – fetch random product from dummyjson.
     * GET https://dummyjson.com/products/random
     */
    suspend fun fetchRandomProduct(): Product? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://dummyjson.com/products/random")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            conn.inputStream.use { input ->
                val body = input.bufferedReader().readText()
                json.decodeFromString(Product.serializer(), body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
//
//@Serializable
//data class Product(val id: Int, val title: String, val description: String)
//
//object DummyJsonRepository {
//    private val json = Json { ignoreUnknownKeys = true }
//
//    fun fetchRandomProduct(): Product? {
//        return try {
//            val content = URL("https://dummyjson.com/products/1").readText()
//            json.decodeFromString<Product>(content)
//        } catch (e: Exception) {
//            null
//        }
//    }
//}