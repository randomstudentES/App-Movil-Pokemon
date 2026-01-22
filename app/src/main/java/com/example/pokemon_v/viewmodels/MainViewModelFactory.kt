
package com.example.pokemon_v.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokemon_v.services.FirestoreService

class MainViewModelFactory(private val firestoreService: FirestoreService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(firestoreService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
