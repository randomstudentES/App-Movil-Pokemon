
package com.example.pokemon_v.models

import com.google.firebase.Timestamp
import java.util.Date

data class LogEntry(
    val userId: String = "",
    val userName: String = "",
    val action: String = "",
    val timestamp: Date = Date()
)
