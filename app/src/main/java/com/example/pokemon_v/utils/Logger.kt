
package com.example.pokemon_v.utils

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

object Logger {
    private val db = Firebase.firestore
    private val logsCollection = db.collection("logs")

    fun log(userId: String, userName: String, action: String) {
        val logEntry = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "action" to action,
            "timestamp" to Calendar.getInstance().time
        )
        logsCollection.add(logEntry)
    }
}
