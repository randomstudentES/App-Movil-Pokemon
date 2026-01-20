package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.ui.composables.api.Team
import com.example.pokemon_v.ui.composables.api.getTeams

@Composable
fun BuscarScreen(
    onInfoClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val teamsState = produceState<List<Team>>(initialValue = emptyList()) {
        value = getTeams()
    }
    val teams = teamsState.value

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
            teams = teams.filter { it.nombre.contains(searchText, ignoreCase = true) },
            onInfoClick = onInfoClick,
            onProfileClick = onProfileClick
        )
    }
}
