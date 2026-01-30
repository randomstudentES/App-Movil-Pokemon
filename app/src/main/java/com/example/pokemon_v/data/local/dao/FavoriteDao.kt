
package com.example.pokemon_v.data.local.dao

import androidx.room.*
import com.example.pokemon_v.data.local.entities.FavoriteTeam
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT teamId FROM favorites WHERE userId = :userId")
    fun getFavoriteTeamIds(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteTeam)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteTeam)

    @Query("DELETE FROM favorites WHERE userId = :userId AND teamId = :teamId")
    suspend fun removeFavorite(userId: String, teamId: String)
}
