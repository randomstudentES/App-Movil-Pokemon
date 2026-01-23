
package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun ParaTiScreen(
    viewModel: MainViewModel,
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val allTeams by viewModel.allTeams.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllTeams()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TeamList(
            teams = allTeams, 
            onInfoClick = onInfoClick,
            onProfileClick = onProfileClick,
            onDeleteClick = {},
            onEditClick = {},
            showEditButton = false,
            showDeleteButton = false
        )
    }
}
