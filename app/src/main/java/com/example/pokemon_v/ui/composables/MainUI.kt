
package com.example.pokemon_v.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokemon_v.R
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.services.FirestoreService
import com.example.pokemon_v.ui.composables.pantallas.*
import com.example.pokemon_v.viewmodels.MainViewModel
import com.example.pokemon_v.viewmodels.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun NavMenuScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Instantiate ViewModel
    val firestoreService = remember { FirestoreService() }
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(firestoreService))
    val currentUser by viewModel.currentUser.collectAsState()

    val fullScreens = listOf("crear", "info_equipo", "lista_pokemon/{index}")
    val showScaffold = currentRoute != null && !fullScreens.any { currentRoute.startsWith(it.split("/")[0]) }

    // --- Delete Confirmation Dialog State ---
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var teamToDelete by remember { mutableStateOf<Equipo?>(null) }
    var teamNameToDelete by remember { mutableStateOf("") }

    val onDeleteRequest = { team: Equipo ->
        teamToDelete = team
        showDeleteConfirmation = true
    }

    if (showDeleteConfirmation && teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar Equipo") },
            text = {
                Column {
                    Text("Para confirmar, escribe el nombre del equipo: '${teamToDelete!!.nombre}'")
                    OutlinedTextField(
                        value = teamNameToDelete,
                        onValueChange = { teamNameToDelete = it },
                        label = { Text("Nombre del equipo") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamNameToDelete == teamToDelete!!.nombre) {
                            viewModel.deleteTeam(currentUser!!.uid, teamToDelete!!.id)
                            showDeleteConfirmation = false
                            teamNameToDelete = ""
                            navController.popBackStack()
                        }
                    },
                    enabled = teamNameToDelete == teamToDelete!!.nombre
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    teamNameToDelete = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            if (showScaffold) {
                MenuInferior(navController, currentRoute, viewModel)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        val modifier = Modifier.padding(if (showScaffold) paddingValues else PaddingValues(0.dp))

        NavHost(
            navController = navController,
            startDestination = "perfil",
            modifier = modifier
        ) {
            composable("perfil") { 
                PerfilScreen(
                    viewModel = viewModel,
                    onCrearClick = { navController.navigate("crear") },
                    onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                    onEditClick = { teamId -> navController.navigate("crear?teamId=$teamId") },
                    onDeleteClick = onDeleteRequest
                ) 
            }
            composable("buscar") { 
                 BuscarScreen(
                    viewModel = viewModel,
                    onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                    onProfileClick = { navController.navigate("perfil") }
                ) 
            }
            composable("para_ti") { 
                 ParaTiScreen(
                    viewModel = viewModel,
                    onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                    onProfileClick = { navController.navigate("perfil") }
                ) 
            }
            composable(
    route = "crear?teamId={teamId}",
    arguments = listOf(navArgument("teamId") { nullable = true })
) {
    if (currentUser != null) {
        val teamId = it.arguments?.getString("teamId")
        CrearScreen(
            navController = navController,
            viewModel = viewModel,
            userId = currentUser!!.uid,
            teamId = teamId,
            onBack = { navController.popBackStack() },
            onAddPokemonClick = { index -> navController.navigate("lista_pokemon/$index") }
        )
    } else {
        // This should not happen if navigation is handled correctly
    }
}
            composable(
                route = "info_equipo/{teamId}",
                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
            ) { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                InfoEquipoScreen(
                    teamId = teamId,
                    viewModel = viewModel,
                    userId = currentUser?.uid ?: "",
                    onBack = { navController.popBackStack() },
                    onEditClick = { teamId -> navController.navigate("crear?teamId=$teamId") },
                    onDeleteClick = onDeleteRequest
                )
            }
            composable(
                route = "lista_pokemon/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                ListaPokemonScreen(
                    onBack = { navController.popBackStack() },
                    onPokemonSelected = { pokemonName ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("selected_pokemon", pokemonName)
                        navController.previousBackStackEntry?.savedStateHandle?.set("target_index", index)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}


@Composable
fun MenuInferior(
    navController: NavHostController, 
    currentRoute: String?, 
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()

    NavigationBar(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(28.dp)),
        containerColor = Color(0xFF1C1C1C),
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("perfil", "Perfil", Icons.Default.Person),
            Triple("buscar", "Buscar", Icons.Default.Search),
            Triple("para_ti", "Para Ti", Icons.Default.Favorite),
            Triple("crear", "Crear", Icons.Default.Add)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            val isCrear = route == "crear"

            NavigationBarItem(
                selected = isSelected,
                label = { Text(label, color = if (isCrear) Color(0xFF2196F3) else if (isSelected) Color.White else Color.Gray) },
                icon = { Icon(icon, contentDescription = label, tint = if (isCrear) Color(0xFF2196F3) else if (isSelected) Color.White else Color.Gray) },
                onClick = {
                    if (route == "crear") {
                        if (currentUser != null) {
                            navController.navigate("crear")
                        } else {
                            navController.navigate("perfil")
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (isCrear) Color(0xFF2196F3).copy(alpha = 0.1f) else Color(0xFF333333)
                )
            )
        }
    }
}

@Composable
fun TeamList(
    teams: List<Equipo>, 
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onDeleteClick: (Equipo) -> Unit, 
    onEditClick: (String) -> Unit,
    showEditButton: Boolean,
    showDeleteButton: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(teams) { team ->
            TeamCard(
                onInfoClick = { onInfoClick(team.id) },
                onProfileClick = onProfileClick,
                nombreEquipo = team.nombre,
                nombreCreador = team.creador,
                showButtonInfo = true,
                onDeleteClick = { onDeleteClick(team) },
                onEditClick = { onEditClick(team.id) },
                showEditButton = showEditButton,
                showDeleteButton = showDeleteButton
            )
        }
    }
}


@Composable
fun GetUserName(userId: String, firestoreService: FirestoreService) {
    var userName by remember { mutableStateOf("...") }

    LaunchedEffect(userId) {
        val user = firestoreService.getUser(userId)
        userName = user?.name ?: "Usuario desconocido"
    }

    Text(
        text = userName,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Gray
    )
}


@Composable
fun TeamCard(
    onInfoClick: () -> Unit, 
    onProfileClick: () -> Unit, 
    nombreEquipo: String, 
    nombreCreador: String, 
    showButtonInfo: Boolean, 
    onEditClick: () -> Unit = {}, 
    showEditButton: Boolean = false, 
    onDeleteClick: () -> Unit = {},
    showDeleteButton: Boolean = false
) {
    var isFavorite by remember { mutableStateOf(false) }
    val firestoreService = remember { FirestoreService() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = "Imagen del equipo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isFavorite) Color.Red else Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Perfil usuario",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onProfileClick() },
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = if (nombreEquipo.length > 10) "${nombreEquipo.take(10)}..." else nombreEquipo,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { onProfileClick() }
                )
                GetUserName(userId = nombreCreador, firestoreService = firestoreService)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                if (showButtonInfo) {
                    OutlinedButton(
                        onClick = onInfoClick,
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 2.dp)
                    ) {
                        Text(
                            text = "info",
                            color = Color.Black
                        )
                    }
                }
    
                if (showEditButton) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 2.dp)
                    ) {
                        Text(
                            text = "Editar",
                            color = Color.Black
                        )
                    }
                }
                if (showDeleteButton) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
