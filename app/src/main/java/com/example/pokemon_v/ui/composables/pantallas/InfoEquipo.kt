package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.ui.composables.TeamCard
import com.example.pokemon_v.ui.composables.api.Team
import com.example.pokemon_v.ui.composables.api.getTeamById

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoEquipoScreen(teamId: Int, onBack: () -> Unit) {
    val teamState = produceState<Team?>(initialValue = null) {
        value = getTeamById(teamId)
    }
    val team = teamState.value

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (team != null) {
                TeamCard(
                    onInfoClick = {},
                    onProfileClick = {},
                    nombreEquipo = team.nombre,
                    nombreCreador = team.creador
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
