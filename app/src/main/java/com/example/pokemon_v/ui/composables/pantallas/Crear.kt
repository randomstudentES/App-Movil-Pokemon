
package com.example.pokemon_v.ui.composables.pantallas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    userId: String,
    onBack: () -> Unit, 
    onAddPokemonClick: (Int) -> Unit
) {
    var teamName by rememberSaveable { mutableStateOf("") }
    var selectedPokemons by rememberSaveable { 
        mutableStateOf(listOf<String?>(null, null, null, null, null, null)) 
    }
    
    var showExitConfirmation by remember { mutableStateOf(false) }

    val handleExitAttempt = {
        if (teamName.isNotBlank() || selectedPokemons.any { it != null }) {
            showExitConfirmation = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = true) {
        handleExitAttempt()
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Si sales ahora, se perderán todos los datos del equipo que has introducido.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmation = false
                        onBack()
                    }
                ) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val resultState = savedStateHandle?.getStateFlow<String?>("selected_pokemon", null)?.collectAsState()
    val targetIndexState = savedStateHandle?.getStateFlow<Int?>("target_index", null)?.collectAsState()

    LaunchedEffect(resultState?.value, targetIndexState?.value) {
        val name = resultState?.value
        val index = targetIndexState?.value
        
        if (name != null && index != null) {
            val newList = selectedPokemons.toMutableList()
            if (index in 0 until 6) {
                newList[index] = name
                selectedPokemons = newList
            }
            savedStateHandle.remove<String>("selected_pokemon")
            savedStateHandle.remove<Int>("target_index")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Crear Equipo") },
                navigationIcon = {
                    IconButton(onClick = handleExitAttempt) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Nombre del equipo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            selectedPokemons.forEachIndexed { index, pokemon ->
                Button(
                    onClick = { onAddPokemonClick(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pokemon != null) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (pokemon != null) Color(0xFF1976D2) else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (pokemon != null) Icons.Default.Check else Icons.Default.Add, 
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = if (pokemon != null) "Pokémon $pokemon" else "Añadir Pokémon")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val newTeam = Equipo(
                        nombre = teamName,
                        creador = userId, // This should be the current user's name or ID
                        pokemons = selectedPokemons.filterNotNull()
                    )
                    viewModel.createTeam(userId, newTeam)
                    navController.navigate("perfil") { 
                        popUpTo("perfil") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = teamName.isNotBlank() && selectedPokemons.any { it != null }
            ) {
                Text("Guardar Equipo")
            }
        }
    }
}
