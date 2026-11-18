package com.zaramoni.cloudaivideogenerator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class RunwayResult(val taskId: String, val videoUrl: String)

class RunwayClient {
    private val client = OkHttpClient()
    private val jsonType = "application/json".toMediaType()
    private val baseUrl = "https://api.dev.runwayml.com"

    suspend fun generate(apiKey: String, prompt: String, duration: Int, ratio: String): RunwayResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("model", "seedance2_5")
                .put("promptText", prompt)
                .put("ratio", ratio)
                .put("duration", duration)
                .toString()

            val request = Request.Builder()
                .url(baseUrl + "/v1/text_to_video")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("X-Runway-Version", "2024-11-06")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw IllegalStateException(parseError(text, "Runway rejected the request (" + response.code + ")."))
                val id = JSONObject(text).optString("id")
                if (id.isBlank()) throw IllegalStateException("Runway did not return a task ID.")
                poll(apiKey, id)
            }
        }

    private suspend fun poll(apiKey: String, taskId: String): RunwayResult {
        repeat(120) {
            val request = Request.Builder()
                .url(baseUrl + "/v1/tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("X-Runway-Version", "2024-11-06")
                .get()
                .build()

            val text = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val value = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw IllegalStateException(parseError(value, "Could not read task status (" + response.code + ")."))
                    value
                }
            }

            val task = JSONObject(text)
            when (task.optString("status")) {
                "SUCCEEDED" -> {
                    val output = task.optJSONArray("output")
                    val url = output?.optString(0).orEmpty()
                    if (url.isBlank()) throw IllegalStateException("Generation succeeded but no video URL was returned.")
                    return RunwayResult(taskId, url)
                }
                "FAILED", "CANCELED" -> throw IllegalStateException(task.optString("failure").ifBlank { "Runway generation failed." })
            }
            delay(5000)
        }
        throw IllegalStateException("Generation timed out. Check your Runway task status.")
    }

    private fun parseError(text: String, fallback: String): String = try {
        val json = JSONObject(text)
        json.optString("error").ifBlank { json.optString("message") }.ifBlank { fallback }
    } catch (_: Exception) { fallback }
}
