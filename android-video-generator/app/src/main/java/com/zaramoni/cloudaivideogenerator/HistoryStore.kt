package com.zaramoni.cloudaivideogenerator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
    val prompt: String,
    val videoUrl: String,
    val createdAt: Long
)

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)

    fun getAll(): List<HistoryItem> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(HistoryItem(o.getString("prompt"), o.getString("videoUrl"), o.getLong("createdAt")))
            }
        }
    }

    fun add(item: HistoryItem) {
        val current = getAll().toMutableList()
        current.add(0, item)
        val array = JSONArray()
        current.take(30).forEach {
            array.put(JSONObject().put("prompt", it.prompt).put("videoUrl", it.videoUrl).put("createdAt", it.createdAt))
        }
        prefs.edit().putString("items", array.toString()).apply()
    }

    fun clear() {
        prefs.edit().remove("items").apply()
    }
}
