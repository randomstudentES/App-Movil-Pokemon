package com.example.pokemon_v.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class AuthService(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(name: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("USER_NAME", name)
            putString("USER_PASSWORD", password)
            apply()
        }
    }

    fun getCredentials(): Pair<String?, String?> {
        val name = sharedPreferences.getString("USER_NAME", null)
        val password = sharedPreferences.getString("USER_PASSWORD", null)
        return Pair(name, password)
    }

    fun clearCredentials() {
        with(sharedPreferences.edit()) {
            remove("USER_NAME")
            remove("USER_PASSWORD")
            apply()
        }
    }
}
