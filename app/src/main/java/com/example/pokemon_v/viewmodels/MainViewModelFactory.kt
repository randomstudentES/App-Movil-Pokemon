
package com.example.pokemon_v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokemon_v.data.local.dao.FavoriteDao
import com.example.pokemon_v.services.AuthService
import com.example.pokemon_v.services.FirestoreService
import com.example.pokemon_v.services.StorageService

class MainViewModelFactory(
    private val firestoreService: FirestoreService,
    private val favoriteDao: FavoriteDao,
    private val authService: AuthService,
    private val storageService: StorageService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(firestoreService, favoriteDao, authService, storageService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
