package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.ui.composables.api.getTeams

@Composable
fun ParaTiScreen(
    onInfoClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val teamsState = produceState<List<Pair<String, String>>>(initialValue = emptyList()) {
        value = getTeams()
    }

    val teams = teamsState.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TeamList(
            teams = teams, // Pasamos la lista de equipos
            onInfoClick = onInfoClick,
            onProfileClick = onProfileClick
        )
    }
}
