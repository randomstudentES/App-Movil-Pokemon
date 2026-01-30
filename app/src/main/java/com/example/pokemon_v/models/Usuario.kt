package com.example.pokemon_v.models

import com.google.firebase.Timestamp

data class Usuario(
    val uid: String = "",
    val name: String = "",
    val description: String? = null,
    val password: String = "",
    val salt: String = "",
    val rol: String = "usuario",
    val created_at: Timestamp = Timestamp.now(),
    val teamIds: List<String> = emptyList()
)
