package com.example.pokemon_v.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(Base64.getDecoder().decode(salt))
        val hashedPassword = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedPassword)
    }
}
