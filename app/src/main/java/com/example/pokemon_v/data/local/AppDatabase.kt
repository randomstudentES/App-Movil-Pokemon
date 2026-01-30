
package com.example.pokemon_v.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pokemon_v.data.local.dao.FavoriteDao
import com.example.pokemon_v.data.local.entities.FavoriteTeam

@Database(entities = [FavoriteTeam::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
