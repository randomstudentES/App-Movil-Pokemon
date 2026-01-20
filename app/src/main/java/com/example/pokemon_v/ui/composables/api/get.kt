package com.example.pokemon_v.ui.composables.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

data class Team(val id: Int, val nombre: String, val creador: String)

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

        val sql = "SELECT e.ID, e.nombre AS equipo_nombre, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID;"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("ID")
            val equipoNombre = resultSet.getString("equipo_nombre")
            val creadorNombre = resultSet.getString("creador_nombre")
            allTeams.add(Team(id, equipoNombre, creadorNombre))
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        val sql = "SELECT e.ID, e.nombre AS equipo_nombre, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID WHERE e.ID = ?"
        preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setInt(1, id)
        resultSet = preparedStatement.executeQuery()

        if (resultSet.next()) {
            val teamId = resultSet.getInt("ID")
            val equipoNombre = resultSet.getString("equipo_nombre")
            val creadorNombre = resultSet.getString("creador_nombre")
            team = Team(teamId, equipoNombre, creadorNombre)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    team
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

        val sql = "SELECT usuario, descripcion FROM usuarios WHERE ID = ?"
        preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setInt(1, 1)

        resultSet = preparedStatement.executeQuery()

        if (resultSet.next()) {
            usuarioNombre = resultSet.getString("usuario")
            usuarioDescripcion = resultSet.getString("descripcion")
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        val sql = "SELECT e.ID, e.nombre AS equipo_nombre, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID WHERE u.ID = 1"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            val id = resultSet.getInt("ID")
            val equipoNombre = resultSet.getString("equipo_nombre")
            val creadorNombre = resultSet.getString("creador_nombre")
            allTeams.add(Team(id, equipoNombre, creadorNombre))
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            resultSet?.close()
            preparedStatement?.close()
            connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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