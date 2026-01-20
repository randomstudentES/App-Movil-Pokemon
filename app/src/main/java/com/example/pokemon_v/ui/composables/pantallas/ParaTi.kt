package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.ui.composables.api.Team
import com.example.pokemon_v.ui.composables.api.getTeams

@Composable
fun ParaTiScreen(
    onInfoClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    // Cambiado de List<Pair<String, String>> a List<Team>
    val teamsState = produceState<List<Team>>(initialValue = emptyList()) {
        value = getTeams()
    }

    val teams = teamsState.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TeamList(
            teams = teams, 
            onInfoClick = onInfoClick,
            onProfileClick = onProfileClick
        )
    }
}
