package com.example.pokemon_v.services

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageService {
    private val storage = FirebaseStorage.getInstance()
    private val profilesRef = storage.reference.child("profiles")

    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): String? {
        return try {
            val fileRef = profilesRef.child("${userId}.jpg")
            fileRef.putFile(imageUri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("StorageService", "Error uploading image to userId: $userId", e)
            null
        }
    }

    suspend fun deleteProfilePicture(userId: String): Boolean {
        return try {
            val fileRef = profilesRef.child("${userId}.jpg")
            fileRef.delete().await()
            true
        } catch (e: Exception) {
            // If file doesn't exist, we consider it deleted/successfully cleared
            true
        }
    }
}
