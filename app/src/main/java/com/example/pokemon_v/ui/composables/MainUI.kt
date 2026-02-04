package com.example.pokemon_v.ui.composables

import android.util.Base64
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
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
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.services.AuthService
import com.example.pokemon_v.services.FirestoreService
import com.example.pokemon_v.services.StorageService
import com.example.pokemon_v.ui.composables.pantallas.*
import com.example.pokemon_v.viewmodels.MainViewModel
import com.example.pokemon_v.viewmodels.MainViewModelFactory
import com.example.pokemon_v.utils.navigateSafe
import coil.compose.AsyncImage
import com.example.pokemon_v.data.local.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun NavMenuScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = androidx.compose.ui.platform.LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }
    val favoriteDao = remember { database.favoriteDao() }

    val firestoreService = remember { FirestoreService() }
    val storageService = remember { StorageService() }
    val authService = remember { AuthService(context) }

    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(firestoreService, favoriteDao, authService, storageService)
    )
    val currentUser by viewModel.currentUser.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            val user = viewModel.currentUser.value
            if (user != null) {
                if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                    viewModel.updateUserOnlineStatus(user.uid, user.online + 1)
                } else if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                    // Logic: If coming back from background, consume the session again.
                    // Note: This assumes ON_STOP fired and incremented it.
                    // We need to be careful not to decrement if we just logged in (which already decremented).
                    // But standard flow: Login (decrements) -> Running.
                    // Stop (increments) -> Background.
                    // Start (decrements) -> Running.
                    // Seems consistent.
                    viewModel.updateUserOnlineStatus(user.uid, user.online - 1)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isAdmin = currentUser?.rol == "admin"

    val mainScreens = remember(isAdmin) {
        if (isAdmin) {
            listOf("perfil", "buscar", "para_ti", "favoritos", "logs", "users")
        } else {
            listOf("perfil", "buscar", "para_ti", "favoritos")
        }
    }

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
                val currentPagerRoute = mainScreens.getOrNull(pagerState.currentPage)
                MenuInferior(
                    navController = navController,
                    currentRoute = currentPagerRoute,
                    viewModel = viewModel,
                    onNavigateToPage = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    mainScreens = mainScreens
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val modifier = Modifier.padding(if (showScaffold) paddingValues else PaddingValues(0.dp))

        val showKickedNotification by viewModel.showKickedNotification.collectAsState()
        if (showKickedNotification) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.dismissKickedNotification()
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                },
                title = { Text("Sesión expirada") },
                text = { Text("Se ha iniciado sesión en otro dispositivo. Tu sesión actual ha sido cerrada.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.dismissKickedNotification()
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }) {
                        Text("Aceptar")
                    }
                }
            )
        }

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
                            onCrearClick = { navController.navigateSafe("crear") },
                            onInfoClick = { teamId -> navController.navigateSafe("info_equipo/$teamId") },
                            onEditClick = { teamId -> navController.navigateSafe("crear?teamId=$teamId") },
                            onDeleteClick = onDeleteRequest
                        )
                        "buscar" -> BuscarScreen(
                            viewModel = viewModel,
                            onInfoClick = { teamId -> navController.navigateSafe("info_equipo/$teamId") },
                            onProfileClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        "para_ti" -> ParaTiScreen(
                            viewModel = viewModel,
                            onInfoClick = { teamId -> navController.navigateSafe("info_equipo/$teamId") },
                            onProfileClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        "favoritos" -> FavoritosScreen(
                            viewModel = viewModel,
                            onInfoClick = { teamId -> navController.navigateSafe("info_equipo/$teamId") },
                            onProfileClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        )
                        "logs" -> LogsScreen(viewModel = viewModel)
                        "users" -> AdminUsersScreen(viewModel = viewModel)
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
                        onAddPokemonClick = { index -> navController.navigateSafe("lista_pokemon/$index") }
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
                    onEditClick = { teamId -> navController.navigateSafe("crear?teamId=$teamId") },
                    onDeleteClick = onDeleteRequest,
                    onProfileClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
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
    onNavigateToPage: (Int) -> Unit,
    mainScreens: List<String>
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser?.rol == "admin"
    var showAdminMenu by remember { mutableStateOf(false) }

    NavigationBar(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(28.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        val items = remember(isAdmin) {
            mutableListOf(
                Triple("perfil", "Perfil", Icons.Default.Person),
                Triple("buscar", "Buscar", Icons.Default.Search),
                Triple("para_ti", "Para Ti", Icons.Default.Slideshow),
                Triple("favoritos", "Favs.", Icons.Default.Favorite)
            ).apply {
                if (isAdmin) {
                    add(Triple("admin", "Admin", Icons.Default.AdminPanelSettings))
                }
                add(Triple("crear", "Crear", Icons.Default.Add))
            }
        }

        items.forEach { (route, label, icon) ->
            if (route == "admin") {
                Box(modifier = Modifier.weight(1f)) {
                    val isAdminRouteSelected = currentRoute == "logs" || currentRoute == "users"
                    this@NavigationBar.NavigationBarItem(
                        selected = isAdminRouteSelected,
                        label = { Text("Admin") },
                        icon = { Icon(icon, contentDescription = label) },
                        onClick = { showAdminMenu = true },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                            selectedTextColor = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = if (isSystemInDarkTheme()) Color.DarkGray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                    DropdownMenu(
                        expanded = showAdminMenu,
                        onDismissRequest = { showAdminMenu = false }
                        // Removed offset to let Compose position it automatically (likely upwards given position)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logs") },
                            onClick = {
                                val logsIndex = mainScreens.indexOf("logs")
                                if (logsIndex != -1) onNavigateToPage(logsIndex)
                                showAdminMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Users") },
                            onClick = {
                                val usersIndex = mainScreens.indexOf("users")
                                if (usersIndex != -1) onNavigateToPage(usersIndex)
                                showAdminMenu = false
                            }
                        )
                    }
                }
            } else {
                val isSelected = currentRoute == route
                val isCrear = route == "crear"
                NavigationBarItem(
                    selected = isSelected,
                    label = { Text(label) },
                    icon = { Icon(icon, contentDescription = label) },
                    onClick = {
                        if (route == "crear") {
                            if (currentUser != null) {
                                navController.navigateSafe("crear")
                            } else {
                                onNavigateToPage(0) // Go to profile if not logged in
                            }
                        } else {
                            val pageIndex = mainScreens.indexOf(route)
                            if (pageIndex != -1) {
                                onNavigateToPage(pageIndex)
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isCrear) Color(0xFF2196F3) else if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                        selectedTextColor = if (isCrear) Color(0xFF2196F3) else if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = if (isCrear) Color(0xFF2196F3).copy(alpha = 0.1f) else (if (isSystemInDarkTheme()) Color.DarkGray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.2f))
                    )
                )
            }
        }
    }
}

@Composable
fun TeamList(
    teams: List<Equipo>,
    favoriteTeamIds: List<String>,
    onFavoriteToggle: (String) -> Unit,
    onInfoClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onDeleteClick: (Equipo) -> Unit,
    onEditClick: (String) -> Unit,
    showEditButton: Boolean,
    showDeleteButton: Boolean,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
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
                showDeleteButton = showDeleteButton,
                isFavorite = favoriteTeamIds.contains(team.id),
                onFavoriteClick = { onFavoriteToggle(team.id) },
                isOwnTeam = team.creador == currentUser?.uid

            )
        }
    }
}

@Composable
fun UserProfileIcon(userId: String, firestoreService: FirestoreService, size: androidx.compose.ui.unit.Dp = 40.dp, onClick: () -> Unit = {}) {
    var user by remember(userId) { mutableStateOf<Usuario?>(null) }
    
    DisposableEffect(userId) {
        val listener = firestoreService.listenToUser(userId) { updatedUser ->
            user = updatedUser
        }
        onDispose {
            listener.remove()
        }
    }

    Box(modifier = Modifier.size(size)) {
        if (user?.profileImageUrl != null) {
            val model = remember(user?.profileImageUrl) {
                val url = user?.profileImageUrl ?: ""
                if (url.startsWith("data:image")) {
                    try {
                        Base64.decode(url.substringAfter("base64,"), Base64.DEFAULT)
                    } catch (e: Exception) {
                        url
                    }
                } else {
                    url
                }
            }
            
            AsyncImage(
                model = model,
                contentDescription = "Perfil usuario",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable { onClick() },
                contentScale = ContentScale.Crop,
                onLoading = { android.util.Log.d("UserProfileIcon", "Icono cargando para $userId") },
                onSuccess = { android.util.Log.d("UserProfileIcon", "Icono cargado con éxito para $userId") },
                onError = { android.util.Log.e("UserProfileIcon", "Error cargando icono para $userId: ${it.result.throwable}") }
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Perfil usuario",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClick() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun CardBackground(background: String, modifier: Modifier = Modifier) {
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

@Composable
fun TeamComposition(pokemonIds: List<String>, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(640f / 480f)
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
fun SlideShow(pokemonIds: List<String>, backgroundName: String, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { pokemonIds.size + 1 })

    Box(modifier = modifier) {
        CardBackground(
            background = backgroundName,
            modifier = Modifier.fillMaxSize()
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                TeamComposition(
                    pokemonIds = pokemonIds,
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
}

@Composable
fun TeamCard(
    onInfoClick: () -> Unit,
    onProfileClick: () -> Unit,
    nombreEquipo: String,
    nombreCreador: String,
    pokemons: List<String> = emptyList(),
    backgroundColor: String = "default_background",
    showButtonInfo: Boolean,
    onEditClick: () -> Unit = {},
    showEditButton: Boolean = false,
    onDeleteClick: () -> Unit = {},
    showDeleteButton: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    isOwnTeam: Boolean = false
) {
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
                    backgroundName = backgroundColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (!isOwnTeam) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserProfileIcon(
                userId = nombreCreador,
                firestoreService = firestoreService,
                size = 40.dp,
                onClick = onProfileClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = if (nombreEquipo.length > 8) "${nombreEquipo.take(8)}..." else nombreEquipo,
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
                        Text(text = "Info", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (showEditButton) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 2.dp)
                    ) {
                        Text(text = "Editar", color = MaterialTheme.colorScheme.onSurface)
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
