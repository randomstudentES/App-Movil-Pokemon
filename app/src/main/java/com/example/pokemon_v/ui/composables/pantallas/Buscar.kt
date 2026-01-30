
package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun BuscarScreen(
    viewModel: MainViewModel,
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val allTeams by viewModel.allTeams.collectAsState()
    val favoriteTeamIds by viewModel.favoriteTeamIds.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllTeams()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Buscar equipos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent
            )
        )

        TeamList(
            teams = allTeams.filter { it.nombre.contains(searchText, ignoreCase = true) },
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
            showDeleteButton = false
        )
    }
}
