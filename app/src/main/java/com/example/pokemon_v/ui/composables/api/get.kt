package com.example.pokemon_v.ui.composables.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

suspend fun getTeams(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon" // 10.0.2.2 es la IP para acceder al localhost desde el emulador
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    val allTeams = mutableListOf<Pair<String, String>>()

    try {
        Class.forName("com.mysql.jdbc.Driver")

        connection = DriverManager.getConnection(url, user, password)
        println("Conexión exitosa a la base de datos")

        val sql = "SELECT e.nombre AS equipo_nombre, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID;"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            val equipoNombre = resultSet.getString("equipo_nombre")
            val creadorNombre = resultSet.getString("creador_nombre")
            allTeams.add(Pair(equipoNombre, creadorNombre))
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

suspend fun getInfoUser(): Pair<String?, String?> {
    return withContext(Dispatchers.IO) {
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
            println("Conexión exitosa a la base de datos")

            val sql = "SELECT usuario, descripcion FROM usuarios WHERE ID = ?"
            preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setInt(1, 1) // Establecer el ID del usuario

            resultSet = preparedStatement.executeQuery()

            if (resultSet.next()) {
                // Solo un resultado se espera, por lo que directamente se obtienen los valores
                usuarioNombre = resultSet.getString("usuario")
                usuarioDescripcion = resultSet.getString("descripcion")
            } else {
                println("No se encontró el usuario con ID: 1")
            }

        } catch (e: Exception) {
            println("Error al obtener información del usuario: ${e.message}")
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
}

suspend fun getTeamsById(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
    val url = "jdbc:mysql://10.0.2.2:3308/pokemon" // 10.0.2.2 es la IP para acceder al localhost desde el emulador
    val user = "root"
    val password = "root"

    var connection: Connection? = null
    var preparedStatement: PreparedStatement? = null
    var resultSet: ResultSet? = null
    val allTeams = mutableListOf<Pair<String, String>>()

    try {
        Class.forName("com.mysql.jdbc.Driver")

        connection = DriverManager.getConnection(url, user, password)
        println("Conexión exitosa a la base de datos")

        val sql = "SELECT e.nombre AS equipo_nombre, u.usuario AS creador_nombre FROM equipos e JOIN usuarios u ON e.ID_creator = u.ID WHERE u.ID = 1"
        preparedStatement = connection.prepareStatement(sql)
        resultSet = preparedStatement.executeQuery()

        while (resultSet.next()) {
            val equipoNombre = resultSet.getString("equipo_nombre")
            val creadorNombre = resultSet.getString("creador_nombre")
            allTeams.add(Pair(equipoNombre, creadorNombre))
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