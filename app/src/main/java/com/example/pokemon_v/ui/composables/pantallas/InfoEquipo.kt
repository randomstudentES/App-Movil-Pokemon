
package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.ui.composables.TeamCard
import com.example.pokemon_v.ui.composables.api.PokemonInfo
import com.example.pokemon_v.ui.composables.api.getPokemonId
import com.example.pokemon_v.ui.composables.api.getPokemonInfo
import com.example.pokemon_v.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoEquipoScreen(
    teamId: String,
    viewModel: MainViewModel,
    userId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Equipo) -> Unit
) {
    val userTeams by viewModel.teams.collectAsState()
    val allTeams by viewModel.allTeams.collectAsState()
    val team = userTeams.find { it.id == teamId } ?: allTeams.find { it.id == teamId }

    var pokemonDetails by remember { mutableStateOf<List<PokemonInfo>>(emptyList()) }

    LaunchedEffect(team) {
        team?.let { t ->
            val details = mutableListOf<PokemonInfo>()
            t.pokemons.forEach { pokemonName ->
                val pokemonId = getPokemonId(pokemonName)
                if (pokemonId != null) {
                    val info = getPokemonInfo(pokemonId)
                    if (info != null) {
                        details.add(info)
                    }
                }
            }
            pokemonDetails = details
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(team?.nombre ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (team != null) {
                TeamCard(
                    onInfoClick = {},
                    onProfileClick = {},
                    nombreEquipo = team.nombre,
                    nombreCreador = team.creador,
                    pokemons = team.pokemons,
                    backgroundColor = team.backgroundColor, // <--- ESTO FALTABA
                    showButtonInfo = false,
                    onEditClick = { onEditClick(team.id) },
                    showEditButton = userId == team.creador,
                    onDeleteClick = { onDeleteClick(team) },
                    showDeleteButton = userId == team.creador
                )

                Text(
                    text = "Integrantes del equipo:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pokemonDetails) { pokemon ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = pokemon.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pokedex ID: ${pokemon.id} | Exp: ${pokemon.baseExperience}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Habilidades: ${pokemon.abilitiesCount} | Movimientos: ${pokemon.movesCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                AsyncImage(
                                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemon.id}.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LaunchedEffect(Unit) {
                    viewModel.loadAllTeams()
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
