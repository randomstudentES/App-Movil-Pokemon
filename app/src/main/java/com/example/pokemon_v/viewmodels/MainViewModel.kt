
package com.example.pokemon_v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.services.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val firestoreService: FirestoreService) : ViewModel() {

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser

    private val _teams = MutableStateFlow<List<Equipo>>(emptyList())
    val teams: StateFlow<List<Equipo>> = _teams

    private val _allTeams = MutableStateFlow<List<Equipo>>(emptyList())
    val allTeams: StateFlow<List<Equipo>> = _allTeams
    
    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError

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
                loadTeams(user.uid)
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
            if (newUser == null) {
                _apiError.value = "El nombre de usuario ya existe."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _teams.value = emptyList()
    }

    fun updateUserDescription(description: String) {
        viewModelScope.launch {
            _currentUser.value?.let {
                firestoreService.updateUserDescription(it.uid, description)
                // Actualizamos el estado local también
                _currentUser.value = it.copy(description = description)
            }
        }
    }

    // Team Functions
    fun createTeam(userId: String, team: Equipo) {
        viewModelScope.launch {
            firestoreService.createTeam(userId, team)
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

    fun updateTeam(userId: String, team: Equipo) {
        viewModelScope.launch {
            firestoreService.updateTeam(userId, team)
            loadTeams(userId)
        }
    }

    fun deleteTeam(userId: String, teamId: String) {
        viewModelScope.launch {
            firestoreService.deleteTeam(userId, teamId)
            loadTeams(userId) // Refresh user's teams
        }
    }
}
