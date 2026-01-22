package com.example.pokemon_v

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pokemon_v.ui.composables.NavMenuScreen
import com.example.pokemon_v.ui.theme.PokemonvTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establece el contenido de la UI usando Jetpack Compose.
        setContent {
            PokemonvTheme {
                // Cargamos la pantalla principal de la UI que contiene la navegación.
                NavMenuScreen()
            }
        }
    }
}
