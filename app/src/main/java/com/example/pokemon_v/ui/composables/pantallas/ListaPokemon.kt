package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemon_v.ui.composables.api.mappers.NameTranslator
import com.example.pokemon_v.utils.readPokemonFromCsv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPokemonScreen(onBack: () -> Unit, onPokemonSelected: (String) -> Unit) {
    val context = LocalContext.current
    val pokemonList = remember { readPokemonFromCsv(context) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = if (searchQuery.isBlank()) {
        pokemonList
    } else {
        pokemonList.filter { (id, name) ->
            val translatedName = NameTranslator.translate(name)
            val formattedId = "#${id.padStart(4, '0')}"
            translatedName.contains(searchQuery, ignoreCase = true) ||
                    formattedId.contains(searchQuery, ignoreCase = true) ||
                    id.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Pokémon") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text(text = "(Ej: \"Bulbasaur\" o \"#0001\")", color = Color.Gray) },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { pokemon ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable { onPokemonSelected(pokemon.first) }, // Pass the ID
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.05f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = NameTranslator.translate(pokemon.second), // Display the translated name
                                modifier = Modifier.align(Alignment.CenterStart),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "#${pokemon.first.padStart(4, '0')}",
                                modifier = Modifier.align(Alignment.CenterEnd),
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
