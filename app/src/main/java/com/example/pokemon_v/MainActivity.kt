package com.example.pokemon_v

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pokemon_v.ui.composables.NavMenuScreen
import com.example.pokemon_v.ui.theme.PokemonvTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = FirebaseFirestore.getInstance()

        // Inicializar colección 'pokemon'
        val pokemonInit = hashMapOf(
            "status" to "active",
            "last_init" to System.currentTimeMillis()
        )
        db.collection("pokemon").document("_setup_").set(pokemonInit)

        // Inicializar colección 'usuario' con el admin
        val adminUser = hashMapOf(
            "id" to 1,
            "usuario" to "admin",
            "contraseña" to "admin"
        )
        
        db.collection("usuario")
            .document("admin_user")
            .set(adminUser)
            .addOnSuccessListener {
                Log.d("Firebase", "Colección 'usuario' inicializada con admin:admin")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al crear colección usuario: ${e.message}")
            }

        setContent {
            PokemonvTheme {
                NavMenuScreen()
            }
        }
    }
}
