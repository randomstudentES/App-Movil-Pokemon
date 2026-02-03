
package com.example.pokemon_v.ui.composables.pantallas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.pokemon_v.R
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.ui.composables.api.mappers.NameTranslator
import com.example.pokemon_v.utils.readPokemonFromCsv
import com.example.pokemon_v.viewmodels.MainViewModel
import com.example.pokemon_v.utils.navigateSafe

@Composable
private fun BackgroundPreview(background: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val color = try {
        Color("FF$background".toLong(16))
    } catch (e: Exception) {
        null
    }

    if (color != null) {
        Box(modifier = modifier.background(color))
    } else {
        val drawableId = remember(background) {
            try {
                val id = context.resources.getIdentifier(background, "drawable", context.packageName)
                if (id == 0) R.drawable.placeholder else id
            } catch (e: Exception) {
                R.drawable.placeholder
            }
        }
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "Background",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    userId: String,
    teamId: String?,
    onBack: () -> Unit,
    onAddPokemonClick: (Int) -> Unit
) {
    var teamName by rememberSaveable { mutableStateOf("") }
    var selectedPokemons by rememberSaveable {
        mutableStateOf(listOf<String?>(null, null, null, null, null, null))
    }
    var selectedBackground by rememberSaveable { mutableStateOf("default_background") }
    var isInitialized by rememberSaveable { mutableStateOf(false) }
    
    var showExitConfirmation by remember { mutableStateOf(false) }
    var backgroundSelectorExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pokemonData = remember { readPokemonFromCsv(context) }
    val teams by viewModel.teams.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState(initial = emptyList())
    val isAdmin = currentUser?.rol == "admin"

    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    var userSelectorExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(allUsers, currentUser) {
        if (isAdmin && selectedUser == null) {
            selectedUser = currentUser
        }
    }

    val backgrounds = remember {
        listOf(
            "bn_n", "dp_trio", "xy_mega", "xy_trio", "bn_munna", "dp_patio", "plt_trio", "rfvh_mar", "rfvh_rio",
            "rze_azul", "bn_sabios", "bn_zekrom", "hgss_alma", "hgss_trio", "roza_aqua", "roza_mapa", "roza_trio",
            "dp_espacio", "dp_pikachu", "dp_torchic", "hgss_pichu", "plt_raichu", "rfvh_cielo", "rfvh_cueva",
            "rfvh_nieve", "rfvh_playa", "roza_magma", "xy_xerneas", "xy_yveltal", "bn_reshiram", "hgss_castor",
            "hgss_kimono", "hgss_rocket", "plt_leyenda", "rfvh_bosque", "rfvh_ciudad", "rfvh_sabana", "rfvh_volcan",
            "xy_conexion", "dp_nostalgia", "hgss_corazon", "plt_concurso", "plt_croagunk", "rfvh_montana",
            "xy_pastelito", "plt_nostalgia", "rfvh_desierto", "roza_concurso", "xy_team_flare", "dp_legendarios",
            "plt_distorsion", "bn_musical_pkmn", "hgss_pokeathlon", "bn_equipo_plasma", "bn_metro_batalla",
            "bn_blanco_y_negro", "dp_equipo_galaxia", "rze_copo_de_nieve", "plt_equipo_galaxia",
            "roza_mega_rayquaza", "bn_zorua_y_zororark", "xy_superentrenamiento", "roza_kyogre_primigenio",
            "roza_groudon_primigenio", "hgss_parque_nacional_dia", "hgss_parque_nacional_noche"
        )
    }

    LaunchedEffect(teamId, teams) {
        if (!isInitialized && teamId != null) {
            val team = teams.find { it.id == teamId }
            if (team != null) {
                teamName = team.nombre
                selectedBackground = team.backgroundColor

                val pokemons: MutableList<String?> = team.pokemons.toMutableList()

                while (pokemons.size < 6) {
                    pokemons.add(null)
                }
                selectedPokemons = pokemons.toList()
                isInitialized = true
            }
        }
    }

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
    val resultState = savedStateHandle?.getStateFlow<String?>("selected_pokemon", null)?.collectAsState(initial = null)
    val targetIndexState = savedStateHandle?.getStateFlow<Int?>("target_index", null)?.collectAsState(initial = null)

    LaunchedEffect(resultState?.value, targetIndexState?.value) {
        val id = resultState?.value
        val index = targetIndexState?.value
        
        if (id != null && index != null) {
            val newList = selectedPokemons.toMutableList()
            if (index in 0 until 6) {
                newList[index] = id
                selectedPokemons = newList
            }
            savedStateHandle?.remove<String>("selected_pokemon")
            savedStateHandle?.remove<Int>("target_index")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (teamId == null) "Crear Equipo" else "Editar Equipo") },
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

            if (isAdmin) {
                ExposedDropdownMenuBox(
                    expanded = userSelectorExpanded,
                    onExpandedChange = { userSelectorExpanded = !userSelectorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.name ?: "",
                        onValueChange = {},
                        label = { Text("Usuario") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userSelectorExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = userSelectorExpanded,
                        onDismissRequest = { userSelectorExpanded = false }
                    ) {
                        allUsers.forEach { user: Usuario ->
                            DropdownMenuItem(
                                text = { Text(user.name) },
                                onClick = {
                                    selectedUser = user
                                    userSelectorExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Nombre del equipo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Pokémon del equipo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            selectedPokemons.forEachIndexed { index, pokemonId ->
                Button(
                    onClick = { onAddPokemonClick(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pokemonId != null) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (pokemonId != null) Color(0xFF1976D2) else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (pokemonId != null) {
                        val pokemonName = pokemonData.find { it.first == pokemonId }?.second ?: "???"
                        val translatedName = NameTranslator.translate(pokemonName)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${pokemonId}.png",
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = translatedName)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Añadir Pokémon")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Background Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Fondo del equipo", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    OutlinedCard(
                        onClick = { backgroundSelectorExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BackgroundPreview(
                                background = selectedBackground,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = selectedBackground, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    if (backgroundSelectorExpanded) {
                        AlertDialog(
                            onDismissRequest = { backgroundSelectorExpanded = false },
                            title = { Text("Seleccionar Fondo") },
                            text = {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.height(200.dp)
                                ) {
                                    items(backgrounds) { bg: String ->
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .padding(4.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .clickable {
                                                    selectedBackground = bg
                                                    backgroundSelectorExpanded = false
                                                }
                                        ) {
                                            BackgroundPreview(background = bg, modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                }
                            },
                            confirmButton = {                            
                                TextButton(onClick = { backgroundSelectorExpanded = false }) {
                                    Text("Cerrar")
                                }
                             }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val creatorId = if (isAdmin) selectedUser?.uid ?: userId else userId
                    val team = Equipo(
                        id = teamId ?: "",
                        nombre = teamName,
                        creador = creatorId, 
                        pokemons = selectedPokemons.filterNotNull(),
                        backgroundColor = selectedBackground
                    )
                    if (teamId == null) {
                        viewModel.createTeam(creatorId, team)
                    } else {
                        viewModel.updateTeam(creatorId, team)
                    }
                    navController.navigateSafe("main") { 
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = teamName.isNotBlank() && selectedPokemons.any { it != null }
            ) {
                Text(if (teamId == null) "Guardar Equipo" else "Actualizar Equipo")
            }
        }
    }
}
