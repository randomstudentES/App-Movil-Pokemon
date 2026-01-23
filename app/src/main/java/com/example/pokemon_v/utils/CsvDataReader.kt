package com.example.pokemon_v.utils

import android.content.Context
import java.io.IOException

fun readPokemonFromCsv(context: Context): List<Pair<String, String>> {
    val pokemonList = mutableListOf<Pair<String, String>>()
    try {
        context.assets.open("pokedex.csv").bufferedReader().useLines { lines ->
            lines.drop(1) // Skip header
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val firstCommaIndex = line.indexOf(',')
                    if (firstCommaIndex != -1) {
                        val id = line.substring(0, firstCommaIndex).trim()
                        val restOfLine = line.substring(firstCommaIndex + 1).trim()

                        val name = if (restOfLine.startsWith("\"")) {
                            val secondQuoteIndex = restOfLine.indexOf('"', 1)
                            if (secondQuoteIndex != -1) {
                                restOfLine.substring(1, secondQuoteIndex)
                            } else {
                                restOfLine.substring(1)
                            }
                        } else {
                            val nextCommaIndex = restOfLine.indexOf(',')
                            if (nextCommaIndex != -1) {
                                restOfLine.substring(0, nextCommaIndex).trim()
                            } else {
                                restOfLine
                            }
                        }
                        pokemonList.add(id to name)
                    }
                }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return pokemonList
}
