
package com.example.pokemon_v.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun NavMenuScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val firestoreService = remember { FirestoreService() }
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(firestoreService))
    val currentUser by viewModel.currentUser.collectAsState()

    val mainScreens = listOf("perfil", "buscar", "para_ti")
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { mainScreens.size })

    val fullScreens = listOf("crear", "info_equipo", "lista_pokemon/{index}")
    val showScaffold = currentRoute != null && !fullScreens.any { currentRoute.startsWith(it.split("/")[0]) }

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
                val currentPagerRoute = mainScreens[pagerState.currentPage]
                MenuInferior(
                    navController = navController,
                    currentRoute = currentPagerRoute,
                    viewModel = viewModel,
                    onNavigateToPage = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        val modifier = Modifier.padding(if (showScaffold) paddingValues else PaddingValues(0.dp))

        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = modifier
        ) {
            composable("main") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (mainScreens[page]) {
                        "perfil" -> PerfilScreen(
                            viewModel = viewModel,
                            onCrearClick = { navController.navigate("crear") },
                            onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                            onEditClick = { teamId -> navController.navigate("crear?teamId=$teamId") },
                            onDeleteClick = onDeleteRequest
                        )
                        "buscar" -> BuscarScreen(
                            viewModel = viewModel,
                            onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                            onProfileClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                        )
                        "para_ti" -> ParaTiScreen(
                            viewModel = viewModel,
                            onInfoClick = { teamId -> navController.navigate("info_equipo/$teamId") },
                            onProfileClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                        )
                    }
                }
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
                    // Handle not logged in case
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
    viewModel: MainViewModel,
    onNavigateToPage: (Int) -> Unit
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

        items.forEachIndexed { index, (route, label, icon) ->
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
                            onNavigateToPage(0)
                        }
                    } else {
                        onNavigateToPage(index)
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
                pokemons = team.pokemons,
                backgroundColor = team.backgroundColor,
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
fun TeamComposition(pokemonIds: List<String>, backgroundColor: String, modifier: Modifier = Modifier) {
    val color = try {
        Color("FF$backgroundColor".toLong(16))
    } catch (e: Exception) {
        Color(0xFFF2F2F2)
    }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(640f / 480f).background(color)
    ) {
        val w = maxWidth
        val h = maxHeight
        val imageSize = w / 5f

        val positions = listOf(
            Pair(60f / 640f, 140f / 480f),
            Pair(140f / 640f, 180f / 480f),
            Pair(220f / 640f, 200f / 480f),
            Pair(300f / 640f, 200f / 480f),
            Pair(380f / 640f, 180f / 480f),
            Pair(460f / 640f, 140f / 480f)
        )

        pokemonIds.take(6).forEachIndexed { index, id ->
            if (id.isNotEmpty()) {
                val (relX, relY) = positions[index]
                AsyncImage(
                    model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png",
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = w * relX, y = h * relY)
                        .size(imageSize),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlideShow(pokemonIds: List<String>, backgroundColor: String, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { pokemonIds.size + 1 })
    val color = try {
        Color("FF$backgroundColor".toLong(16))
    } catch (e: Exception) {
        Color(0xFFF2F2F2)
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.background(color)
    ) { page ->
        if (page == 0) {
            TeamComposition(
                pokemonIds = pokemonIds,
                backgroundColor = backgroundColor,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val pokemonId = pokemonIds[page - 1]
            AsyncImage(
                model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}


@Composable
fun TeamCard(
    onInfoClick: () -> Unit, 
    onProfileClick: () -> Unit, 
    nombreEquipo: String, 
    nombreCreador: String, 
    pokemons: List<String> = emptyList(),
    backgroundColor: String = "f2f2f2",
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
            if (pokemons.isEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Imagen del equipo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                SlideShow(
                    pokemonIds = pokemons,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

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
