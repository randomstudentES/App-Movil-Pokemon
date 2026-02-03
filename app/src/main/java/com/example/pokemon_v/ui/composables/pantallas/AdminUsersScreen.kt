package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun AdminUsersScreen(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text("Manage Users", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        }
        items(users) { user ->
            UserManagementCard(user = user, viewModel = viewModel)
            Divider()
        }
    }
}

@Composable
fun UserManagementCard(user: Usuario, viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "UID: ${user.uid}", style = MaterialTheme.typography.bodySmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val newOnlineValue = user.online - 1
                viewModel.updateUserOnlineStatus(user.uid, newOnlineValue)
            }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease online count")
            }
            Text(text = user.online.toString(), style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = {
                val newOnlineValue = user.online + 1
                viewModel.updateUserOnlineStatus(user.uid, newOnlineValue)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Increase online count")
            }
        }
    }
}
