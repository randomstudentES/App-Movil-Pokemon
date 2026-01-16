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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokemon_v.R
import com.example.pokemon_v.ui.composables.api.getTeams
import com.example.pokemon_v.ui.composables.pantallas.*

@Preview(showBackground = true)
@Composable
fun NavMenuScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ocultar Scaffold en pantallas de flujo específico para que sean a pantalla completa
    val fullScreens = listOf("crear", "info_equipo", "lista_pokemon/{index}")
    val showScaffold = currentRoute != null && !fullScreens.any { currentRoute!!.startsWith(it.split("/")[0]) }
    
    val showTopBar = currentRoute == "info_equipo"

    Scaffold(
        topBar = {
            if (showTopBar) {
                // InfoEquipo maneja su propia TopBar
            }
        },
        bottomBar = {
            if (showScaffold) {
                MenuInferior(navController, currentRoute)
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
                    onCrearClick = { navController.navigate("crear") },
                    onInfoClick = { navController.navigate("info_equipo") }
                ) 
            }
            composable("buscar") { 
                BuscarScreen(
                    onInfoClick = { navController.navigate("info_equipo") },
                    onProfileClick = { navController.navigate("perfil") }
                ) 
            }
            composable("para_ti") { 
                ParaTiScreen(
                    onInfoClick = { navController.navigate("info_equipo") },
                    onProfileClick = { navController.navigate("perfil") }
                ) 
            }
            composable("crear") {
                CrearScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onAddPokemonClick = { index -> navController.navigate("lista_pokemon/$index") }
                )
            }
            composable("info_equipo") {
                InfoEquipoScreen(onBack = { navController.popBackStack() })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cabecera() {
    CenterAlignedTopAppBar(
        title = { Text("Pokemon App") },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun MenuInferior(navController: NavHostController, currentRoute: String?) {
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
                label = {
                    Text(
                        text = label,
                        color = if (isCrear) Color(0xFF2196F3) else if (isSelected) Color.White else Color.Gray
                    )
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isCrear) Color(0xFF2196F3) else if (isSelected) Color.White else Color.Gray
                    )
                },
                onClick = {
                    if (route == "crear") {
                        navController.navigate("crear")
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
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
    teams: List<Pair<String, String>>, // Aceptamos la lista por parámetro
    onInfoClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(teams.size) { index ->
            // Desestructuramos el Pair (nombre, creador)
            val (equipoNombre, creadorNombre) = teams[index]

            TeamCard(
                onInfoClick = onInfoClick,
                onProfileClick = onProfileClick,
                nombreEquipo = equipoNombre,
                nombreCreador = creadorNombre
            )
        }
    }
}


@Composable
fun TeamCard(onInfoClick: () -> Unit, onProfileClick: () -> Unit, nombreEquipo: String, nombreCreador: String) {
    var isFavorite by remember { mutableStateOf(false) }

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
                Text(
                    text = nombreCreador,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onProfileClick() },
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

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
