package com.example.pokemon_v.ui.composables.api.mappers

object TypeTranslator {
    private val typeNameToSpanish = mapOf(
        "normal" to "normal",
        "fire" to "fuego",
        "water" to "agua",
        "electric" to "electrico",
        "grass" to "planta",
        "ice" to "hielo",
        "fighting" to "lucha",
        "poison" to "veneno",
        "ground" to "tierra",
        "flying" to "volador",
        "psychic" to "psiquico",
        "bug" to "bicho",
        "rock" to "roca",
        "ghost" to "fantasma",
        "dragon" to "dragon",
        "dark" to "siniestro",
        "steel" to "acero",
        "fairy" to "hada"
    )

    fun translate(typeInEnglish: String): String {
        return typeNameToSpanish[typeInEnglish.lowercase()] ?: "???"
    }
}
