package com.example.data.security

import java.security.MessageDigest

object SecurityUtils {

    /**
     * Hashes a password string using SHA-256 with a salt prefix.
     */
    fun hashPassword(password: String): String {
        val salted = "RSLIBRARY_SALT_2026_$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies that the provided plain password matches the stored hash.
     */
    fun verifyPassword(plainPassword: String, storedHash: String): Boolean {
        if (plainPassword.isEmpty() || storedHash.isEmpty()) return false
        val computedHash = hashPassword(plainPassword)
        return computedHash.equals(storedHash, ignoreCase = true)
    }

    /**
     * Default Admin credentials
     */
    const val ADMIN_USERNAME = "Raman1998"
    val ADMIN_PASSWORD_HASH: String = hashPassword("Rahul328650@#")
}
