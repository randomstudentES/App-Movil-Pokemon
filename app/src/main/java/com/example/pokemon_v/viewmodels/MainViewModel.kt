package com.example.pokemon_v.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemon_v.data.local.dao.FavoriteDao
import com.example.pokemon_v.data.local.entities.FavoriteTeam
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.LogEntry
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.services.AuthService
import com.example.pokemon_v.services.FirestoreService
import com.example.pokemon_v.services.StorageService
import com.example.pokemon_v.utils.Logger
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val firestoreService: FirestoreService,
    private val favoriteDao: FavoriteDao,
    private val authService: AuthService,
    private val storageService: StorageService
) : ViewModel() {

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser

    private val _teams = MutableStateFlow<List<Equipo>>(emptyList())
    val teams: StateFlow<List<Equipo>> = _teams

    private val _allTeams = MutableStateFlow<List<Equipo>>(emptyList())
    val allTeams: StateFlow<List<Equipo>> = _allTeams

    private val _allUsers = MutableStateFlow<List<Usuario>>(emptyList())
    val allUsers: StateFlow<List<Usuario>> = _allUsers

    private val _searchResults = MutableStateFlow<List<Equipo>>(emptyList())
    val searchResults: StateFlow<List<Equipo>> = _searchResults
    
    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError

    // State for Logs
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs

    private val _isUploadingProfilePic = MutableStateFlow(false)
    val isUploadingProfilePic: StateFlow<Boolean> = _isUploadingProfilePic

    private var lastLogsSnapshot: QuerySnapshot? = null
    private var isLastLogsPage = false

    // Favorites from Room
    val favoriteTeamIds: StateFlow<List<String>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else favoriteDao.getFavoriteTeamIds(user.uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearApiError() {
        _apiError.value = null
    }

    fun getFirestoreService(): FirestoreService = firestoreService

    private val _showLoginConfirmation = MutableStateFlow(false)
    val showLoginConfirmation: StateFlow<Boolean> = _showLoginConfirmation

    private val _showKickedNotification = MutableStateFlow(false)
    val showKickedNotification: StateFlow<Boolean> = _showKickedNotification
    
    // Auth Functions
    private var sessionListener: ListenerRegistration? = null
    private var currentSessionId: String? = null
    private var pendingLoginData: Pair<String, String>? = null

    fun login(name: String, password: String) {
        viewModelScope.launch {
            _apiError.value = null
            _showLoginConfirmation.value = false
            
            val user = firestoreService.login(name, password)
            if (user != null) {
                if (user.online > 0) {
                     // Empty slots available, proceed
                     performLogin(user)
                } else {
                    // No slots. Prompt to kick oldest.
                    pendingLoginData = Pair(name, password)
                    _showLoginConfirmation.value = true
                }
            } else {
                _apiError.value = "Nombre de usuario o contraseña incorrectos."
            }
        }
    }

    fun confirmLogin() {
        viewModelScope.launch {
            _showLoginConfirmation.value = false
            if (pendingLoginData != null) {
                val (name, password) = pendingLoginData!!
                val user = firestoreService.login(name, password)
                if (user != null) {
                     // Kick oldest logic is handled inside performLogin which calls updateSessionList
                     performLogin(user, kickOldest = true)
                }
                pendingLoginData = null
            }
        }
    }
    
    fun cancelLogin() {
        _showLoginConfirmation.value = false
        pendingLoginData = null
        _apiError.value = "Inicio de sesión cancelado."
    }

    private suspend fun performLogin(user: Usuario, kickOldest: Boolean = false) {
        val newSessionId = java.util.UUID.randomUUID().toString()
        currentSessionId = newSessionId
        
        val timestamp = System.currentTimeMillis()
        val sessionData = mapOf(
            "id" to newSessionId,
            "timestamp" to timestamp,
            "device" to "Android Device" // Placeholder
        )
        
        var currentSessions = user.sessions.toMutableList()
        var currentOnline = user.online

        if (kickOldest && currentSessions.isNotEmpty()) {
             // Sort by timestamp and remove oldest
             currentSessions.sortBy { (it["timestamp"] as? Long) ?: 0L }
             currentSessions.removeAt(0)
             // note: online count stays 0 (busy) because we replaced one.
             // But if currentOnline was somehow > 0, we decrement.
             // Logic: kickOldest implies online=0.
        } else {
             // Normal login, decrement online
             if (currentOnline > 0) currentOnline -= 1
        }
        
        currentSessions.add(sessionData)
        
        // Update Firestore
        firestoreService.updateUserSessionList(user.uid, currentOnline, currentSessions)
        
        val updatedUser = user.copy(online = currentOnline, sessions = currentSessions, sessionId = newSessionId) // sessionId legacy support
        _currentUser.value = updatedUser
        
        Logger.log(user.uid, user.name, "Inicio de sesión")
        
        loadTeams(user.uid)
        if (user.rol == "admin") {
            loadAllUsers()
        }
        
        startSessionListener(user.uid, newSessionId)
    }

    private fun startSessionListener(userId: String, localSessionId: String) {
        sessionListener?.remove()
        sessionListener = firestoreService.listenToUser(userId) { updatedUser ->
            if (updatedUser != null) {
                // Check if our session ID is still in the sessions list
                val sessions = updatedUser.sessions
                val isMySessionActive = sessions.any { it["id"] == localSessionId }
                
                if (!isMySessionActive) {
                    // Kick!
                    logout(kicked = true)
                }
            }
        }
    }

    fun register(user: Usuario) {
        viewModelScope.launch {
            _apiError.value = null
            val newUser = firestoreService.register(user)
            if (newUser != null) {
                // Register logic same as login basically
                 performLogin(newUser)
                
                Logger.log(newUser.uid, newUser.name, "Registro de usuario e inicio de sesión")
            } else {
                _apiError.value = "El nombre de usuario ya existe."
            }
        }
    }

    fun logout(kicked: Boolean = false) {
        val user = _currentUser.value
        sessionListener?.remove()
        sessionListener = null
        val mySessionId = currentSessionId
        currentSessionId = null
        
        viewModelScope.launch {
            user?.let {
                if (!kicked && mySessionId != null) {
                    // Update session list: remove my session, increment online
                    val currentSessions = it.sessions.toMutableList()
                    currentSessions.removeAll { session -> session["id"] == mySessionId }
                    val newOnline = it.online + 1
                    
                    firestoreService.updateUserSessionList(it.uid, newOnline, currentSessions)
                }
                Logger.log(it.uid, it.name, if (kicked) "Cierre de sesión (Expulsado)" else "Cierre de sesión")
            }
        }
        _currentUser.value = null
        _teams.value = emptyList()
        
        if (kicked) {
            _showKickedNotification.value = true
            _apiError.value = "Se ha iniciado sesión en otro dispositivo."
        }
    }

    fun dismissKickedNotification() {
        _showKickedNotification.value = false
    }
    
    fun getTeamById(teamId: String): Equipo? {
        val userTeams = _teams.value
        val allTeams = _allTeams.value
        return userTeams.find { it.id == teamId } ?: allTeams.find { it.id == teamId }
    }

    fun updateUserDescription(description: String) {
        viewModelScope.launch {
            _currentUser.value?.let {
                firestoreService.updateUserDescription(it.uid, description)
                Logger.log(it.uid, it.name, "Actualización de descripción de perfil")
                // Actualizamos el estado local también
                _currentUser.value = it.copy(description = description)
            }
        }
    }

    fun uploadProfilePicture(uri: android.net.Uri, context: android.content.Context) {
        val user = _currentUser.value ?: return
        android.util.Log.d("MainViewModel", "Iniciando subida de foto para usuario ${user.uid} con URI: $uri")
        viewModelScope.launch {
            _isUploadingProfilePic.value = true
            
            // Intentamos primero con Storage (mejor práctica)
            val url = storageService.uploadProfilePicture(user.uid, uri)
            
            if (url != null) {
                android.util.Log.d("MainViewModel", "Subida a Storage exitosa. URL: $url")
                val success = firestoreService.updateUserProfilePicture(user.uid, url)
                if (success) {
                    val updatedUser = user.copy(profileImageUrl = url)
                    _currentUser.value = updatedUser
                    android.util.Log.d("MainViewModel", "Estado local actualizado (Storage). URL start: ${url.take(50)}... Len: ${url.length}")
                    Logger.log(user.uid, user.name, "Cambio de foto de perfil (Storage)")
                    Toast.makeText(context, "Foto de perfil actualizada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    android.util.Log.e("MainViewModel", "Error al actualizar Firestore con el link de Storage")
                    Toast.makeText(context, "Error al guardar el link en la base de datos", Toast.LENGTH_LONG).show()
                }
            } else {
                android.util.Log.w("MainViewModel", "Storage falló. Intentando guardado directo en Firestore (Base64)...")
                
                // Fallback: Guardado directo en Firestore como Base64
                val base64 = convertUriToBase64(uri, context)
                if (base64 != null) {
                    val dataUrl = "data:image/jpeg;base64,$base64"
                    val success = firestoreService.updateUserProfilePicture(user.uid, dataUrl)
                    if (success) {
                        val updatedUser = user.copy(profileImageUrl = dataUrl)
                        _currentUser.value = updatedUser
                        android.util.Log.d("MainViewModel", "Estado local actualizado (Directo). DataURL start: ${dataUrl.take(50)}... Len: ${dataUrl.length}")
                        Logger.log(user.uid, user.name, "Cambio de foto de perfil (Directo)")
                        Toast.makeText(context, "Foto guardada directamente en la base de datos", Toast.LENGTH_SHORT).show()
                    } else {
                        android.util.Log.e("MainViewModel", "Error al actualizar Firestore con Base64")
                        Toast.makeText(context, "Error al guardar imagen directa en la base de datos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    android.util.Log.e("MainViewModel", "Error: Fallaron ambos métodos de guardado.")
                    _apiError.value = "Error al subir la imagen. Verifica tu conexión o configuración."
                    Toast.makeText(context, "Error total al subir la imagen", Toast.LENGTH_LONG).show()
                }
            }
            _isUploadingProfilePic.value = false
        }
    }

    private fun convertUriToBase64(uri: android.net.Uri, context: android.content.Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap != null) {
                // Redimensionamos a un tamaño razonable para icono de perfil (ej. 400x400)
                val size = 400
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, size, true)
                
                val outputStream = java.io.ByteArrayOutputStream()
                // Comprimimos como JPEG al 70% de calidad para ahorrar mucho espacio
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                
                android.util.Log.d("MainViewModel", "Imagen comprimida. Tamaño final: ${bytes.size} bytes")
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error convirtiendo y comprimiendo imagen a Base64", e)
            null
        }
    }

    fun deleteProfilePicture() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isUploadingProfilePic.value = true
            val success = storageService.deleteProfilePicture(user.uid)
            if (success) {
                firestoreService.updateUserProfilePicture(user.uid, null)
                _currentUser.value = user.copy(profileImageUrl = null)
                Logger.log(user.uid, user.name, "Eliminación de foto de perfil")
            }
            _isUploadingProfilePic.value = false
        }
    }

    // Team Functions
    fun createTeam(userId: String, team: Equipo) {
        val currentUser = _currentUser.value
        val isAdmin = currentUser?.rol == "admin"
        
        viewModelScope.launch {
            firestoreService.createTeam(userId, team)
            
            currentUser?.let {
                if (isAdmin && userId != it.uid) {
                    // Ghost log for admin creating for another user
                    val targetUser = _allUsers.value.find { it.uid == userId }
                    Logger.log(it.uid, it.name, "Admin creó equipo [${team.nombre}] para [${targetUser?.name ?: userId}]")
                } else {
                    Logger.log(it.uid, it.name, "Creación de equipo: ${team.nombre}")
                }
            }
            
            // Refresh logic:
            // 1. Refresh global teams if admin (so change is seen in Para Ti / Buscar)
            if (isAdmin) loadAllTeams()
            
            // 2. Refresh profile teams to ensure NO ghosts are left
            currentUser?.let { 
                loadTeams(it.uid, true) 
            }
        }
    }

    fun loadTeams(userId: String, forceLoad: Boolean = false) {
        val currentUserId = _currentUser.value?.uid
        if (userId != currentUserId && currentUserId != null) {
            // Protect _teams from being populated with other users' teams
            return
        }
        
        viewModelScope.launch {
            if (forceLoad || _teams.value.isEmpty()) {
                _teams.value = firestoreService.getTeams(userId)
            }
        }
    }

    fun loadAllTeams() {
        viewModelScope.launch {
            val allTeamsList = firestoreService.getAllTeams()
            val currentUserId = _currentUser.value?.uid

            // Filtramos la lista: solo incluimos equipos donde el creadorId NO sea el del usuario actual
            _allTeams.value = if (currentUserId != null) {
                allTeamsList.filter { it.creador != currentUserId }
            } else {
                allTeamsList
            }
        }
    }

    fun searchTeams(query: String) {
        viewModelScope.launch {
            _searchResults.value = firestoreService.searchTeams(query)
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            if (_currentUser.value?.rol == "admin") {
                _allUsers.value = firestoreService.getAllUsers()
            }
        }
    }

    fun updateUserOnlineStatus(userId: String, newOnlineValue: Int) {
        viewModelScope.launch {
            firestoreService.updateUserOnlineStatus(userId, newOnlineValue)
            
            // Log admin action if applicable
            _currentUser.value?.let { admin ->
                if (admin.rol == "admin" && admin.uid != userId) {
                    val targetUser = _allUsers.value.find { it.uid == userId }
                    Logger.log(admin.uid, admin.name, "Cambio de sesiones [${targetUser?.name ?: userId}] a $newOnlineValue")
                }
            }

            // Update local state if it's the current user
            val current = _currentUser.value
            if (current != null && current.uid == userId) {
                _currentUser.value = current.copy(online = newOnlineValue)
            }
            
            loadAllUsers()
        }
    }

    fun updateTeam(userId: String, team: Equipo, oldCreatorId: String? = null) {
        val currentUser = _currentUser.value
        val isAdmin = currentUser?.rol == "admin"

        viewModelScope.launch {
            firestoreService.updateTeam(userId, team, isAdmin, oldCreatorId)
            
            currentUser?.let {
                if (isAdmin && userId != it.uid) {
                    // Ghost log for admin editing another user's team
                    val targetUser = _allUsers.value.find { it.uid == userId }
                    Logger.log(it.uid, it.name, "Admin editó equipo [${team.nombre}] de [${targetUser?.name ?: userId}]")
                } else {
                    Logger.log(it.uid, it.name, "Edición de equipo: ${team.nombre}")
                }
            }
            
            // Refresh logic:
            // 1. Refresh global teams if admin
            if (isAdmin) loadAllTeams()
            
            // 2. Refresh profile teams to ensure NO ghosts are left
            currentUser?.let { 
                loadTeams(it.uid, true) 
            }
        }
    }

    fun deleteTeam(userId: String, teamId: String) {
        val currentUser = _currentUser.value
        val isAdmin = currentUser?.rol == "admin"

        viewModelScope.launch {
            val teamName = _allTeams.value.find { it.id == teamId }?.nombre 
                ?: _teams.value.find { it.id == teamId }?.nombre 
                ?: "ID: $teamId"
                
            firestoreService.deleteTeam(userId, teamId)
            
            currentUser?.let {
                if (isAdmin && userId != it.uid) {
                    val targetUser = _allUsers.value.find { it.uid == userId }
                    Logger.log(it.uid, it.name, "Admin eliminó equipo [$teamName] de [${targetUser?.name ?: userId}]")
                } else {
                    Logger.log(it.uid, it.name, "Eliminación de equipo: $teamName")
                }
            }
            
            // Refresh logic same as creation/update
            if (isAdmin) loadAllTeams()
            
            currentUser?.let { 
                loadTeams(it.uid, true) 
            }
        }
    }

    // Logs Functions
    fun loadLogs(reset: Boolean = false) {
        if (_isLoadingLogs.value || (isLastLogsPage && !reset)) return

        viewModelScope.launch {
            _isLoadingLogs.value = true
            if (reset) {
                lastLogsSnapshot = null
                _logs.value = emptyList()
                isLastLogsPage = false
            }

            val snapshot = firestoreService.getLogs(lastLogsSnapshot)
            if (snapshot != null) {
                val newLogs = snapshot.toObjects(LogEntry::class.java)
                _logs.value = _logs.value + newLogs
                lastLogsSnapshot = snapshot
                if (newLogs.size < 50) {
                    isLastLogsPage = true
                }
            }
            _isLoadingLogs.value = false
        }
    }

    fun deleteAllLogs() {
        val currentUser = _currentUser.value
        if (currentUser?.rol == "admin") {
            viewModelScope.launch {
                firestoreService.deleteAllLogs()
                Logger.log(currentUser.uid, currentUser.name, "Admin eliminó todos los logs")
                loadLogs(reset = true)
            }
        }
    }

    // Favorite Functions
    fun toggleFavorite(teamId: String) {
        val userId = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            val favorites = favoriteTeamIds.value
            if (favorites.contains(teamId)) {
                favoriteDao.removeFavorite(userId, teamId)
            } else {
                favoriteDao.insertFavorite(FavoriteTeam(userId, teamId))
            }
        }
    }
}
