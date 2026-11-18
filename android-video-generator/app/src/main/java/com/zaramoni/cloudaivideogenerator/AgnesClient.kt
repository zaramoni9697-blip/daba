package com.zaramoni.cloudaivideogenerator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class AgnesResult(val videoId: String, val videoUrl: String)

class AgnesClient {
    private val client = OkHttpClient()
    private val jsonType = "application/json".toMediaType()
    private val baseUrl = "https://apihub.agnes-ai.com"

    suspend fun generate(
        apiKey: String,
        prompt: String,
        duration: Int,
        ratio: String
    ): AgnesResult = withContext(Dispatchers.IO) {
        require(duration in 4..12) { "Agnes Flash supports 4–12 second videos." }

        val json = JSONObject()
            .put("model", "agnes-video-2.5-flash")
            .put("prompt", prompt)
            .put("mode", "text")
            .put("seconds", duration.toString())
            .put("size", "720P")
            .put("aspect_ratio", ratio)
            .put("n", 1)

        var lastError = "Agnes AI video generation failed."
        repeat(3) { attempt ->
            val request = Request.Builder()
                .url(baseUrl + "/v1/videos")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val body = JSONObject(text)
                    val videoId = firstNonBlank(
                        body.optString("video_id"),
                        body.optString("id"),
                        body.optString("task_id"),
                        body.optJSONObject("data")?.optString("video_id"),
                        body.optJSONObject("data")?.optString("id")
                    )
                    if (videoId.isBlank()) throw IllegalStateException("Agnes AI did not return a video ID.")
                    return@withContext poll(apiKey, videoId)
                }
                lastError = parseError(text, "Agnes AI rejected the request (" + response.code + ").")
                if (response.code != 408 && response.code != 429 && response.code != 500 &&
                    response.code != 502 && response.code != 503 && response.code != 504) {
                    throw IllegalStateException(lastError)
                }
            }
            delay((attempt + 1) * 5000L)
        }
        throw IllegalStateException(lastError)
    }

    private suspend fun poll(apiKey: String, videoId: String): AgnesResult {
        repeat(180) {
            val url = baseUrl + "/agnesapi?video_id=" + java.net.URLEncoder.encode(videoId, "UTF-8") +
                "&model_name=agnes-video-2.5-flash"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build()

            val text = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    val value = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IllegalStateException(parseError(value, "Could not read Agnes AI video status (" + response.code + ")."))
                    }
                    value
                }
            }

            val body = JSONObject(text)
            when (body.optString("status").lowercase()) {
                "completed", "succeeded", "success" -> {
                    val videoUrl = firstNonBlank(
                        body.optString("url"),
                        body.optJSONObject("data")?.optString("url"),
                        body.optJSONObject("result")?.optString("url")
                    )
                    if (videoUrl.isBlank()) throw IllegalStateException("Generation completed but Agnes AI returned no video URL.")
                    return AgnesResult(videoId, videoUrl)
                }
                "failed", "error", "canceled", "cancelled" -> {
                    throw IllegalStateException(
                        firstNonBlank(body.optString("message"), body.optString("error"), "Agnes AI generation failed.")
                    )
                }
            }
            delay(5000)
        }
        throw IllegalStateException("Generation timed out. Please try again later.")
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }.orEmpty()

    private fun parseError(text: String, fallback: String): String = try {
        val json = JSONObject(text)
        firstNonBlank(
            json.optString("message"),
            json.optString("error"),
            json.optString("code"),
            json.optJSONObject("error")?.optString("message"),
            fallback
        )
    } catch (_: Exception) {
        fallback
    }
}
