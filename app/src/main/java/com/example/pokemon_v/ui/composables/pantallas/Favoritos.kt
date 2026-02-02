
package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun FavoritosScreen(
    viewModel: MainViewModel,
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val allTeams by viewModel.allTeams.collectAsState()
    val favoriteTeamIds by viewModel.favoriteTeamIds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Inicia sesión para ver tus equipos favoritos.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onProfileClick) {
                Text("Iniciar Sesión")
            }
        }
    } else {
        val favoriteTeams = allTeams.filter { favoriteTeamIds.contains(it.id) }

        if (favoriteTeams.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TeamList(
                    teams = favoriteTeams,
                    favoriteTeamIds = favoriteTeamIds,
                    onFavoriteToggle = { teamId ->
                        viewModel.toggleFavorite(teamId)
                    },
                    onInfoClick = onInfoClick,
                    onProfileClick = onProfileClick,
                    onDeleteClick = {},
                    onEditClick = {},
                    showEditButton = false,
                    showDeleteButton = false
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
                Text("No tienes equipos guardados en favoritos.")
            }
        }
    }
}
