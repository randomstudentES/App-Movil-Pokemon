package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.pokemon_v.ui.composables.TeamList

@Composable
fun ParaTiScreen(
    onInfoClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TeamList(onInfoClick = onInfoClick, onProfileClick = onProfileClick)
    }
}
