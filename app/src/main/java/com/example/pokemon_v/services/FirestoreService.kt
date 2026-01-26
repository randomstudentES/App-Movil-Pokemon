
package com.example.pokemon_v.services

import android.util.Log
import com.example.pokemon_v.models.Equipo
import com.example.pokemon_v.models.Usuario
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreService {

    private val db: FirebaseFirestore = Firebase.firestore
    private val usuariosCollection = db.collection("usuarios")
    private val equiposCollection = db.collection("equipos")

    // Auth operations
    suspend fun login(name: String, password: String): Usuario? {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("name", name)
                .whereEqualTo("password", password) // Insecure!
                .limit(1)
                .get()
                .await()
            snapshot.toObjects(Usuario::class.java).firstOrNull()
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
                val newUser = user.copy(uid = userId)
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

    suspend fun updateUser(user: Usuario) {
        if (user.uid.isNotEmpty()) {
            try {
                // Usamos update para evitar sobrescribir todo el objeto si solo queremos actualizar campos básicos
                // Pero como este método recibe el objeto completo, si se quiere ser precavido es mejor usar updates específicos
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
            // Create a document reference with a new ID in the 'equipos' collection
            val teamDocument = equiposCollection.document()
            // Create a new team object that includes the generated ID
            val teamWithId = team.copy(id = teamDocument.id, creador = userId)
            // Set the new team object in the document
            teamDocument.set(teamWithId).await()
            
            // Add the new team's ID to the user's list of teamIds
            usuariosCollection.document(userId).update("teamIds", FieldValue.arrayUnion(teamWithId.id)).await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error creating team", e)
        }
    }

    suspend fun getTeams(userId: String): List<Equipo> {
        try {
            val user = getUser(userId)
            if (user != null && user.teamIds.isNotEmpty()) {
                // Fetch all team documents where the document ID is in the user's teamIds list.
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
            // Delete the team document from the 'equipos' collection
            equiposCollection.document(teamId).delete().await()
            // Remove the teamId from the user's 'teamIds' list
            usuariosCollection.document(userId).update("teamIds", FieldValue.arrayRemove(teamId)).await()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error deleting team", e)
        }
    }
}
