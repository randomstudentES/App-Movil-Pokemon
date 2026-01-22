package com.example.pokemon_v.models

import com.google.firebase.firestore.DocumentId

data class Equipo(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val creador: String = "",
    val pokemons: List<String> = emptyList()
)