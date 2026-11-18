@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.zaramoni.cloudaivideogenerator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VideoGeneratorApp() }
    }
}

@Composable
fun VideoGeneratorApp() {
    val context = LocalContext.current
    val securePrefs = remember { SecurePrefs(context) }
    val historyStore = remember { HistoryStore(context) }
    val scope = rememberCoroutineScope()

    var prompt by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(5) }
    var quality by remember { mutableStateOf("720p") }
    var ratio by remember { mutableStateOf("16:9") }
    var draft by remember { mutableStateOf(false) }
    var audio by remember { mutableStateOf(true) }
    var apiKey by remember { mutableStateOf(securePrefs.getApiKey()) }
    var history by remember { mutableStateOf(historyStore.getAll()) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var generating by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cloud AI Video") },
                    actions = {
                        TextButton(onClick = { showHistory = true }) { Text("History") }
                        TextButton(onClick = { showSettings = true }) { Text("Settings") }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Text → Video", style = MaterialTheme.typography.headlineMedium)
                Text("Create videos with Agnes Video 2.5 Flash (free tier).")

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Prompt") },
                    placeholder = { Text("A cinematic drone shot over a futuristic city at sunset...") },
                    minLines = 5
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SimpleDropdown("Duration", "$duration sec", (4..12).map { "$it sec" },
                        { duration = it.removeSuffix(" sec").toInt() }, Modifier.weight(1f))
                    SimpleDropdown("Ratio", ratio, listOf("16:9", "9:16", "1:1", "4:3", "3:4"),
                        { ratio = it }, Modifier.weight(1f))
                }

                SimpleDropdown(
                    "Quality",
                    quality,
                    listOf("720p"),
                    { quality = it },
                    Modifier.fillMaxWidth()
                )

                Text(
                    "Free Flash mode supports 720p and 4–12 second videos. The free API is rate-limited and availability can vary.",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        error = ""; videoUrl = ""
                        if (apiKey.isBlank()) {
                            error = "Open Settings and add your Agnes AI API key first."
                            return@Button
                        }
                        generating = true
                        status = "Generating video. This may take a few minutes..."
                        scope.launch {
                            try {
                                val selectedRatio = ratio
                                val result = AgnesClient().generate(
                                    apiKey = apiKey,
                                    prompt = prompt.trim(),
                                    duration = duration,
                                    ratio = selectedRatio
                                )
                                videoUrl = result.videoUrl
                                historyStore.add(HistoryItem(prompt.trim(), result.videoUrl, System.currentTimeMillis()))
                                history = historyStore.getAll()
                                status = "Video generated successfully."
                            } catch (e: Exception) {
                                status = ""
                                error = e.message ?: "Generation failed."
                            } finally {
                                generating = false
                            }
                        }
                    },
                    enabled = prompt.isNotBlank() && !generating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (generating) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Generate Video")
                }

                if (status.isNotBlank()) Text(status)
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

                if (videoUrl.isNotBlank()) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Your video is ready.", style = MaterialTheme.typography.titleMedium)
                            Button({ openVideo(context, videoUrl) }, Modifier.fillMaxWidth()) { Text("Open Video") }
                            OutlinedButton({
                                scope.launch {
                                    try {
                                        status = "Saving video..."
                                        saveVideo(context, videoUrl)
                                        status = "Video saved to Movies/Cloud AI Video."
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not save video."
                                    }
                                }
                            }, Modifier.fillMaxWidth()) { Text("Save to Device") }
                            OutlinedButton({ shareVideo(context, videoUrl) }, Modifier.fillMaxWidth()) { Text("Share Link") }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showSettings) SettingsDialog(
        initialKey = apiKey,
        onDismiss = { showSettings = false },
        onSave = {
            securePrefs.setApiKey(it)
            apiKey = it
            showSettings = false
            status = "API key saved securely on this device."
        },
        onClear = {
            securePrefs.clearApiKey()
            apiKey = ""
            showSettings = false
        }
    )

    if (showHistory) HistoryDialog(
        items = history,
        onDismiss = { showHistory = false },
        onOpen = { openVideo(context, it) },
        onClear = { historyStore.clear(); history = emptyList() }
    )
}

@Composable
private fun SimpleDropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton({ expanded = true }, Modifier.fillMaxWidth()) { Text(value) }
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
private fun SettingsDialog(initialKey: String, onDismiss: () -> Unit, onSave: (String) -> Unit, onClear: () -> Unit) {
    var value by remember { mutableStateOf(initialKey) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Your Agnes AI API key is encrypted on this device.")
                OutlinedTextField(
                    value = value, onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Agnes AI API key") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation()
                )
                Text("The key is sent directly to Agnes AI over HTTPS.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = { onSave(value) }, enabled = value.isNotBlank()) { Text("Save") } },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun HistoryDialog(items: List<HistoryItem>, onDismiss: () -> Unit, onOpen: (String) -> Unit, onClear: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generation History") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (items.isEmpty()) Text("No generated videos yet.")
                items.forEach { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.prompt, maxLines = 3)
                            Text(DateFormat.getDateTimeInstance().format(Date(item.createdAt)),
                                style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { onOpen(item.videoUrl) }) { Text("Open") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        dismissButton = { if (items.isNotEmpty()) TextButton(onClick = onClear) { Text("Clear history") } }
    )
}

private fun openVideo(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun shareVideo(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, "Share video"))
}

private suspend fun saveVideo(context: Context, url: String) = withContext(Dispatchers.IO) {
    val response = OkHttpClient().newCall(Request.Builder().url(url).get().build()).execute()
    response.use {
        if (!it.isSuccessful) throw IllegalStateException("Video download failed (" + it.code + ").")
        val body = it.body ?: throw IllegalStateException("Empty video response.")
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "cloud_ai_video_" + System.currentTimeMillis() + ".mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Cloud AI Video")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Could not create a media file.")
        try {
            resolver.openOutputStream(uri)?.use { output -> body.byteStream().use { input -> input.copyTo(output) } }
                ?: throw IllegalStateException("Could not open output file.")
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }
}
