
package com.example.pokemon_v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemon_v.data.local.dao.FavoriteDao
import com.example.pokemon_v.data.local.entities.FavoriteTeam
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.LogEntry
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.services.FirestoreService
import com.example.pokemon_v.utils.Logger
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val firestoreService: FirestoreService,
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser

    private val _teams = MutableStateFlow<List<Equipo>>(emptyList())
    val teams: StateFlow<List<Equipo>> = _teams

    private val _allTeams = MutableStateFlow<List<Equipo>>(emptyList())
    val allTeams: StateFlow<List<Equipo>> = _allTeams

    private val _allUsers = MutableStateFlow<List<Usuario>>(emptyList())
    val allUsers: StateFlow<List<Usuario>> = _allUsers
    
    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError

    // State for Logs
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs

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

    // Auth Functions
    fun login(name: String, password: String) {
        viewModelScope.launch {
            _apiError.value = null
            val user = firestoreService.login(name, password)
            _currentUser.value = user
            if (user != null) {
                Logger.log(user.uid, user.name, "Inicio de sesión")
                loadTeams(user.uid)
                if (user.rol == "admin") {
                    loadAllUsers()
                }
            } else {
                _apiError.value = "Nombre de usuario o contraseña incorrectos."
            }
        }
    }

    fun register(user: Usuario) {
        viewModelScope.launch {
            _apiError.value = null
            val newUser = firestoreService.register(user)
            _currentUser.value = newUser
            if (newUser != null) {
                Logger.log(newUser.uid, newUser.name, "Registro de usuario e inicio de sesión")
            } else {
                _apiError.value = "El nombre de usuario ya existe."
            }
        }
    }

    fun logout() {
        _currentUser.value?.let { 
            Logger.log(it.uid, it.name, "Cierre de sesión")
        }
        _currentUser.value = null
        _teams.value = emptyList()
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

    // Team Functions
    fun createTeam(userId: String, team: Equipo) {
        viewModelScope.launch {
            firestoreService.createTeam(userId, team)
            _currentUser.value?.let {
                Logger.log(it.uid, it.name, "Creación de equipo: ${team.nombre}")
            }
            loadTeams(userId) // Refresh user's teams
        }
    }

    fun loadTeams(userId: String) {
        viewModelScope.launch {
            _teams.value = firestoreService.getTeams(userId)
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

    fun loadAllUsers() {
        viewModelScope.launch {
            if (_currentUser.value?.rol == "admin") {
                _allUsers.value = firestoreService.getAllUsers()
            }
        }
    }

    fun updateTeam(userId: String, team: Equipo) {
        viewModelScope.launch {
            firestoreService.updateTeam(userId, team)
            _currentUser.value?.let {
                Logger.log(it.uid, it.name, "Edición de equipo: ${team.nombre}")
            }
            loadTeams(userId)
        }
    }

    fun deleteTeam(userId: String, teamId: String) {
        viewModelScope.launch {
            val teamName = _teams.value.find { it.id == teamId }?.nombre ?: "ID: $teamId"
            firestoreService.deleteTeam(userId, teamId)
            _currentUser.value?.let {
                Logger.log(it.uid, it.name, "Eliminación de equipo: $teamName")
            }
            loadTeams(userId) // Refresh user's teams
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
