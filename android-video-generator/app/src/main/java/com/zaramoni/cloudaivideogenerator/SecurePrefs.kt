package com.zaramoni.cloudaivideogenerator

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefs(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val prefs = EncryptedSharedPreferences.create(
        context, "secure_settings", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    fun getApiKey(): String = prefs.getString("runway_api_key", "") ?: ""
    fun setApiKey(value: String) { prefs.edit().putString("runway_api_key", value.trim()).apply() }
    fun clearApiKey() { prefs.edit().remove("runway_api_key").apply() }
}
