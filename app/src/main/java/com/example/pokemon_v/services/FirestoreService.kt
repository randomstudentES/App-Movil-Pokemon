
package com.example.pokemon_v.services

import android.util.Log
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.LogEntry
import com.example.pokemon_v.models.Usuario
import com.example.pokemon_v.utils.SecurityUtils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreService {

    private val db: FirebaseFirestore = Firebase.firestore
    private val usuariosCollection = db.collection("usuarios")
    private val equiposCollection = db.collection("equipos")
    private val logsCollection = db.collection("logs")

    // Auth operations
    suspend fun login(name: String, password: String): Usuario? {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .await()
            
            val user = snapshot.toObjects(Usuario::class.java).firstOrNull()
            if (user != null) {
                val hashedAttempt = SecurityUtils.hashPassword(password, user.salt)
                if (hashedAttempt == user.password) {
                    user
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error logging in", e)
            null
        }
    }

    suspend fun register(user: Usuario): Usuario? {
        return try {
            // Check if username already exists
            val existingUser = usuariosCollection.whereEqualTo("name", user.name).get().await()
            if (existingUser.isEmpty) {
                val userId = UUID.randomUUID().toString()
                
                // Hash the password before saving
                val salt = SecurityUtils.generateSalt()
                val hashedPassword = SecurityUtils.hashPassword(user.password, salt)
                
                // CAMBIO: Aseguramos que el rol sea "usuario" por defecto al registrarse
                val newUser = user.copy(
                    uid = userId, 
                    password = hashedPassword,
                    salt = salt,
                    rol = "usuario"
                )
                
                usuariosCollection.document(userId).set(newUser).await()
                newUser
            } else {
                null // Username already exists
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error registering user", e)
            null
        }
    }


    // User operations
    suspend fun createUser(user: Usuario) {
        usuariosCollection.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): Usuario? {
        return try {
            usuariosCollection.document(uid).get().await().toObject(Usuario::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getting user", e)
            null
        }
    }

    suspend fun getAllUsers(): List<Usuario> {
        return try {
            usuariosCollection.get().await().toObjects(Usuario::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getting all users", e)
            emptyList()
        }
    }

    suspend fun updateUser(user: Usuario) {
        if (user.uid.isNotEmpty()) {
            try {
                usuariosCollection.document(user.uid).set(user).await()
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error updating user", e)
            }
        }
    }

    suspend fun updateUserDescription(uid: String, description: String) {
        if (uid.isNotEmpty()) {
            try {
                usuariosCollection.document(uid).update("description", description).await()
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error updating description", e)
            }
        }
    }

    // Team operations
    suspend fun createTeam(userId: String, team: Equipo) {
        try {
            val teamDocument = equiposCollection.document()
            val teamWithId = team.copy(id = teamDocument.id, creador = userId)
            teamDocument.set(teamWithId).await()
            usuariosCollection.document(userId).update("teamIds", FieldValue.arrayUnion(teamWithId.id)).await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error creating team", e)
        }
    }

    suspend fun getTeams(userId: String): List<Equipo> {
        try {
            val user = getUser(userId)
            if (user != null && user.teamIds.isNotEmpty()) {
                val teamsSnapshot = equiposCollection.whereIn(FieldPath.documentId(), user.teamIds).get().await()
                return teamsSnapshot.toObjects(Equipo::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getting teams for user", e)
        }
        return emptyList()
    }

    suspend fun getAllTeams(): List<Equipo> {
        return try {
            equiposCollection.limit(50).get().await().toObjects(Equipo::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getting all teams", e)
            emptyList()
        }
    }

    suspend fun updateTeam(userId: String, team: Equipo) {
        if (team.id.isNotEmpty() && team.creador == userId) {
            try {
                equiposCollection.document(team.id).set(team).await()
            } catch (e: Exception) {
                Log.e("FirestoreService", "Error updating team", e)
            }
        }
    }

    suspend fun deleteTeam(userId: String, teamId: String) {
        try {
            equiposCollection.document(teamId).delete().await()
            usuariosCollection.document(userId).update("teamIds", FieldValue.arrayRemove(teamId)).await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error deleting team", e)
        }
    }

    // Logs operations
    suspend fun getLogs(lastSnapshot: QuerySnapshot? = null): QuerySnapshot? {
        return try {
            var query = logsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
            
            if (lastSnapshot != null && !lastSnapshot.isEmpty) {
                val lastVisible = lastSnapshot.documents[lastSnapshot.size() - 1]
                query = query.startAfter(lastVisible)
            }
            
            query.get().await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error getting logs", e)
            null
        }
    }
}
