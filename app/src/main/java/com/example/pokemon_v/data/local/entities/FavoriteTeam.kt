
package com.example.pokemon_v.data.local.entities

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["userId", "teamId"])
data class FavoriteTeam(
    val userId: String,
    val teamId: String
)
