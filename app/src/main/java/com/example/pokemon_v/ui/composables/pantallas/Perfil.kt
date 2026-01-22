
package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun PerfilScreen(
    viewModel: MainViewModel,
    onCrearClick: () -> Unit, 
    onInfoClick: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    if (currentUser == null) {
        LoginRegisterScreen(viewModel = viewModel)
    } else {
        ProfileView(viewModel = viewModel, onCrearClick = onCrearClick, onInfoClick = onInfoClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    viewModel: MainViewModel,
    onCrearClick: () -> Unit, 
    onInfoClick: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val teams by viewModel.teams.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            viewModel.loadTeams(user.uid)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(currentUser?.name ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Opciones de perfil",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text("Logout") }, onClick = { viewModel.logout() })
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentUser?.description ?: "",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = { /* Modificar descripción */ },
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Modificar", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCrearClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear nuevo equipo")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tus equipos:",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )

            TeamList(
                teams = teams,
                onInfoClick = onInfoClick,
                onProfileClick = { }
            )
        }
    }

    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            confirmButton = {
                TextButton(onClick = { alertMessage = null }) {
                    Text("OK")
                }
            },
            text = { Text(alertMessage!!) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(viewModel: MainViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var apiError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    fun validatePassword(pass: String): String? {
        if (pass.length <= 8) return "La contraseña debe tener más de 8 caracteres."
        if (!pass.any { it.isUpperCase() }) return "Debe contener una mayúscula."
        if (!pass.any { it.isDigit() }) return "Debe contener un número."
        if (!pass.any { !it.isLetterOrDigit() }) return "Debe contener un carácter especial."
        return null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (isLogin) "Iniciar Sesión" else "Registro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre de usuario") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (!isLogin) {
                    passwordError = validatePassword(it)
                }
            },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = !isLogin && password.isNotEmpty() && passwordError != null,
            supportingText = {
                if (!isLogin && password.isNotEmpty() && passwordError != null) {
                    passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )
        if (!isLogin) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción (opcional)") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                apiError = null
                if (isLogin) {
                    viewModel.login(name, password)
                } else {
                    val newUser = Usuario(name = name, password = password, description = description)
                    viewModel.register(newUser)
                }
            },
            enabled = if (isLogin) name.isNotBlank() && password.isNotBlank()
                      else name.isNotBlank() && password.isNotBlank() && passwordError == null
        ) {
            Text(if (isLogin) "Entrar" else "Registrarse")
        }

        apiError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isLogin) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión",
            modifier = Modifier.clickable { isLogin = !isLogin },
            color = MaterialTheme.colorScheme.primary
        )
    }
}
