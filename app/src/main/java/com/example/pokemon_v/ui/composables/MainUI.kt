package com.example.pokemon_v.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pokemon_v.ui.composables.pantallas.BuscarScreen
import com.example.pokemon_v.ui.composables.pantallas.CrearScreen
import com.example.pokemon_v.ui.composables.pantallas.ParaTiScreen
import com.example.pokemon_v.ui.composables.pantallas.PerfilScreen

@Preview(showBackground = true)
@Composable
fun NavMenuScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ocultar Scaffold en la pantalla de "crear" para que sea a pantalla completa
    val showScaffold = currentRoute != "crear"

    Scaffold(
        topBar = {
            if (showScaffold) {
                Cabecera()
            }
        },
        bottomBar = {
            if (showScaffold) {
                MenuInferior(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        // Aplicar el padding del Scaffold solo si se muestra la barra inferior
        val modifier = Modifier.padding(if (showScaffold) paddingValues else PaddingValues(0.dp))

        NavHost(
            navController = navController,
            startDestination = "perfil",
            modifier = modifier
        ) {
            composable("perfil") { PerfilScreen() }
            composable("buscar") { BuscarScreen() }
            composable("para_ti") { ParaTiScreen() }
            composable("crear") {
                CrearScreen(onBack = {
                    // Al volver, se destruye la pantalla de crear del stack
                    navController.popBackStack()
                })
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
fun MenuInferior(navController: NavHostController, currentRoute: String?) {
    NavigationBar(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp) // Flota a una distancia prudente
            .clip(RoundedCornerShape(28.dp)),
        containerColor = Color(0xFF1C1C1C),
        tonalElevation = 8.dp
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
                            // Navega al destino inicial para evitar acumular pantallas
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