package com.example.pokemon_v.ui.composables.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemon_v.ui.composables.TeamCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onCrearClick: () -> Unit, onInfoClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Nombre Usuario", fontWeight = FontWeight.Bold) },
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
                            text = { Text("Cambiar foto de perfil") },
                            onClick = {
                                showMenu = false
                                alertMessage = "Aún no se ha implementado"
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar foto de perfil") },
                            onClick = {
                                showMenu = false
                                alertMessage = "Se ha eliminado la foto de perfil"
                            }
                        )
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
            // Descripción
            Text(
                text = "Descripcion: blablabla blablablablablabla",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Botón pequeño para modificar descripción
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

            // Botón crear nuevo equipo (todo el ancho)
            Button(
                onClick = onCrearClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear nuevo equipo")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Una sola TeamCard como ejemplo
            Text(
                text = "Tus equipos:",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            
            TeamCard(
                onInfoClick = onInfoClick,
                onProfileClick = { /* Ya estamos en perfil */ }
            )
        }
    }

    // Ventana de mensaje (Alerta)
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
