package com.example.pokemon_v.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.navigateSafe(
    route: String,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        if (builder != null) {
            this.navigate(route, builder)
        } else {
            this.navigate(route)
        }
    }
}
