package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun ParaTiScreen(
    viewModel: MainViewModel,
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val allTeams by viewModel.allTeams.collectAsState()
    val favoriteTeamIds by viewModel.favoriteTeamIds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllTeams()
    }

    if (allTeams.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TeamList(
                teams = allTeams,
                favoriteTeamIds = favoriteTeamIds,
                onFavoriteToggle = { teamId -> 
                    if (currentUser == null) {
                        onProfileClick()
                    } else {
                        viewModel.toggleFavorite(teamId)
                    }
                },
                onInfoClick = onInfoClick,
                onProfileClick = onProfileClick,
                onDeleteClick = {},
                onEditClick = {},
                showEditButton = false,
                showDeleteButton = false,
                viewModel = viewModel
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No hay ningun equipo, espera a ver si crea alguien alguno :p")
        }
    }
}
