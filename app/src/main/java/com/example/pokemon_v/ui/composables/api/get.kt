package com.example.pokemon_v.ui.composables.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

data class Team(
    val id: Int, 
    val nombre: String, 
    val creador: String, 
    val pokemonIds: List<Int?> = emptyList()
)

data class PokemonInfo(
    val id: Int,
    val name: String,
    val baseExperience: Int,
    val abilitiesCount: Int,
    val movesCount: Int,
    val imageUrl: String
)

private val client = OkHttpClient()
private val gson = Gson()

// NEW FUNCTION to get ID from name
suspend fun getPokemonId(pokemonName: String): Int? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/${pokemonName.lowercase()}")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val jsonObject = gson.fromJson(body, JsonObject::class.java)
            jsonObject.get("id").asInt
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun getPokemonInfo(pokemonId: Int): PokemonInfo? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url("https://pokeapi.co/api/v2/pokemon/$pokemonId")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val jsonObject = gson.fromJson(body, JsonObject::class.java)

            val id = jsonObject.get("id").asInt
            val name = jsonObject.get("name").asString
            val baseExperience = jsonObject.get("base_experience").asInt
            val abilitiesCount = jsonObject.getAsJsonArray("abilities").size()
            val movesCount = jsonObject.getAsJsonArray("moves").size()
            
            // Imagen por defecto de la API
            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

            PokemonInfo(id, name, baseExperience, abilitiesCount, movesCount, imageUrl)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun getTeams(): List<Team> = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon"
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    val allTeams = mutableListOf<Team>()

    try {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(url, user, password)

        val sql = "SELECT e.*, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID;"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            allTeams.add(mapResultSetToTeam(resultSet))
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        resultSet?.close()
        preparedStatement?.close()
        connection?.close()
    }
    allTeams
}

suspend fun getTeamById(id: Int): Team? = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon"
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    var team: Team? = null

    try {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(url, user, password)

        val sql = "SELECT e.*, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID WHERE e.ID = ?"
        preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setInt(1, id)
        resultSet = preparedStatement.executeQuery()

        if (resultSet.next()) {
            team = mapResultSetToTeam(resultSet)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        resultSet?.close()
        preparedStatement?.close()
        connection?.close()
    }
    team
}

private fun mapResultSetToTeam(rs: ResultSet): Team {
    val ids = mutableListOf<Int?>()
    for (i in 1..6) {
        val pId = rs.getInt("id_pokemon$i")
        ids.add(if (rs.wasNull()) null else pId)
    }
    return Team(
        id = rs.getInt("ID"),
        nombre = rs.getString("nombre"),
        creador = rs.getString("creador_nombre"),
        pokemonIds = ids
    )
}

suspend fun getInfoUser(): Pair<String?, String?> = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon"
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    var usuarioNombre: String? = null
    var usuarioDescripcion: String? = null

    try {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(url, user, password)
        val sql = "SELECT usuario, descripcion FROM usuarios WHERE ID = 1"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        if (resultSet.next()) {
            usuarioNombre = resultSet.getString("usuario")
            usuarioDescripcion = resultSet.getString("descripcion")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        resultSet?.close()
        preparedStatement?.close()
        connection?.close()
    }
    Pair(usuarioNombre, usuarioDescripcion)
}

suspend fun getTeamsById(): List<Team> = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon"
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    val allTeams = mutableListOf<Team>()

    try {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(url, user, password)

        val sql = "SELECT e.*, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID WHERE u.ID = 1"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            allTeams.add(mapResultSetToTeam(resultSet))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        resultSet?.close()
        preparedStatement?.close()
        connection?.close()
    }
    allTeams
}

suspend fun saveTeam(nombre: String, creatorId: Int, pokemonIds: List<String?>): Int? = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon"
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var generatedId: Int? = null

    try {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(url, user, password)

        val sql = "INSERT INTO equipos (nombre, ID_creator, id_pokemon1, id_pokemon2, id_pokemon3, id_pokemon4, id_pokemon5, id_pokemon6) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        preparedStatement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
        
        preparedStatement.setString(1, nombre)
        preparedStatement.setInt(2, creatorId)
        
        for (i in 0 until 6) {
            val pId = if (i < pokemonIds.size) pokemonIds[i] else null
            if (pId != null) {
                preparedStatement.setInt(i + 3, pId.toInt())
            } else {
                preparedStatement.setNull(i + 3, java.sql.Types.INTEGER)
            }
        }

        val affectedRows = preparedStatement.executeUpdate()
        if (affectedRows > 0) {
            val rs = preparedStatement.generatedKeys
            if (rs.next()) {
                generatedId = rs.getInt(1)
            }
            rs.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        preparedStatement?.close()
        connection?.close()
    }
    generatedId
}
