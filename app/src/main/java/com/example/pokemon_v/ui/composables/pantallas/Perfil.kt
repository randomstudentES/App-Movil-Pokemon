
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
import androidx.lint.kotlin.metadata.Visibility
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.ui.composables.TeamList
import com.example.pokemon_v.viewmodels.MainViewModel

@Composable
fun PerfilScreen(
    viewModel: MainViewModel,
    onCrearClick: () -> Unit, 
    onInfoClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Equipo) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    if (currentUser == null) {
        LoginRegisterScreen(viewModel = viewModel)
    } else {
        ProfileView(
            viewModel = viewModel, 
            onCrearClick = onCrearClick, 
            onInfoClick = onInfoClick, 
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    viewModel: MainViewModel,
    onCrearClick: () -> Unit, 
    onInfoClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Equipo) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val teams by viewModel.teams.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isEditingDescription by remember { mutableStateOf(false) }
    var descriptionText by remember { mutableStateOf(currentUser?.description ?: "") }
    var showNotImplementedDialog by remember { mutableStateOf(false) }

    if (showNotImplementedDialog) {
        AlertDialog(
            onDismissRequest = { showNotImplementedDialog = false },
            text = { Text("Por implementar") },
            confirmButton = {
                TextButton(onClick = { showNotImplementedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

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
                        DropdownMenuItem(
                            text = { Text("Modificar foto de perfil") }, 
                            onClick = { 
                                showNotImplementedDialog = true
                                showMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar foto de perfil") }, 
                            onClick = { 
                                showNotImplementedDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(text = { Text("Cerrar Sesión") }, onClick = { 
                            viewModel.logout()
                            showMenu = false
                        })
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
            if (isEditingDescription) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Tu descripción") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Button(onClick = { 
                        viewModel.updateUserDescription(descriptionText)
                        isEditingDescription = false
                    }) {
                        Text("Guardar")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { 
                        isEditingDescription = false
                        descriptionText = currentUser?.description ?: ""
                    }) {
                        Text("Cancelar")
                    }
                }
            } else {
                Text(
                    text = currentUser?.description ?: "",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedButton(
                    onClick = { isEditingDescription = true },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Modificar descripción", fontSize = 12.sp)
                }
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
                onProfileClick = {},
                onDeleteClick = onDeleteClick,
                onEditClick = onEditClick,
                showEditButton = false,
                showDeleteButton = true
            )
        }
    }

    alertMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            confirmButton = {
                TextButton(onClick = { alertMessage = null }) {
                    Text("OK")
                }
            },
            text = { Text(message) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(viewModel: MainViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val apiError by viewModel.apiError.collectAsState()
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    fun validatePassword(pass: String): String? {
        if (pass.length <= 8) return "La contraseña debe tener más de 8 caracteres."
        if (!pass.any { it.isUpperCase() }) return "Debe contener una mayúscula."
        if (!pass.any { it.isDigit() }) return "Debe contener un número."
        if (!pass.any { !it.isLetterOrDigit() }) return "Debe contener un carácter especial."
        return null
    }

    fun validateConfirmPassword(pass1: String, pass2: String): String? {
        if (pass1 != pass2) return "Las contraseñas no coinciden."
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

        val isPasswordError = !isLogin && password.isNotEmpty() && passwordError != null
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (!isLogin) {
                    passwordError = validatePassword(it)
                    if (confirmPassword.isNotEmpty()) {
                        confirmPasswordError = validateConfirmPassword(it, confirmPassword)
                    }
                }
            },
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError) {
                    passwordError?.let { Text(it) }
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

            val isConfirmPasswordError = !isLogin && confirmPassword.isNotEmpty() && confirmPasswordError != null
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    confirmPasswordError = validateConfirmPassword(password, it)
                },
                label = { Text("Confirmar contraseña") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = isConfirmPasswordError,
                supportingText = {
                    if (isConfirmPasswordError) {
                        confirmPasswordError?.let { Text(it) }
                    }
                },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLogin) {
                    viewModel.login(name, password)
                } else {
                    val newUser = Usuario(name = name, password = password, description = "")
                    viewModel.register(newUser)
                }
            },
            enabled = if (isLogin) name.isNotBlank() && password.isNotBlank()
                      else name.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && passwordError == null && confirmPasswordError == null
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
            modifier = Modifier.clickable { 
                isLogin = !isLogin 
            },
            color = MaterialTheme.colorScheme.primary
        )
    }
}
