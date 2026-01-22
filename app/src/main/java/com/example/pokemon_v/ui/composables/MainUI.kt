
package com.example.pokemon_v.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    val showScaffold = currentRoute != null && !fullScreens.any { currentRoute!!.startsWith(it.split("/")[0]) }

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
                    onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") }
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
            composable("crear") {
                if (currentUser != null) {
                    CrearScreen(
                        navController = navController,
                        viewModel = viewModel,
                        userId = currentUser!!.uid,
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
                    onBack = { navController.popBackStack() })
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
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(teams.size) { index ->
            val team = teams[index]

            TeamCard(
                onInfoClick = { onInfoClick(team.id) },
                onProfileClick = onProfileClick,
                nombreEquipo = team.nombre,
                nombreCreador = team.creador,
                showButtonInfo = true
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
fun TeamCard(onInfoClick: () -> Unit, onProfileClick: () -> Unit, nombreEquipo: String, nombreCreador: String, showButtonInfo: Boolean) {
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
                    text = nombreEquipo,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { onProfileClick() }
                )
                GetUserName(userId = nombreCreador, firestoreService = firestoreService)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showButtonInfo) {
                OutlinedButton(
                    onClick = onInfoClick,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                ) {
                    Text(
                        text = "info",
                        color = Color.Black
                    )
                }
            }

        }
    }
}
