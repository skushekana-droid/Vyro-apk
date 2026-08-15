package com.example.infrastructure.auth

import com.example.model.UserRole
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class VyroUserSession(
    val sessionId: String = "sess_${UUID.randomUUID().toString().take(10)}",
    val userId: String,
    val userEmail: String,
    val role: UserRole,
    val deviceName: String = "Android Client",
    val ipAddress: String = "192.168.1.100",
    val userAgent: String = "VYRO-Mobile/2.4 (Android 14; Pixel 8)",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days
    val isRevoked: Boolean = false
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long = 3600,
    val session: VyroUserSession
)

data class AuthResult(
    val success: Boolean,
    val message: String,
    val tokens: AuthTokens? = null,
    val requiresTwoFactor: Boolean = false,
    val twoFactorMethod: String? = null,
    val temporarySessionId: String? = null
)

object VyroPasswordHasher {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val input = "$salt:$password:vyro_pepper_secret_9941"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computed = hashPassword(password, salt)
        return computed == expectedHash
    }
}

interface VyroAuthService {
    val providerName: String
    val isIndependentProvider: Boolean

    suspend fun register(
        email: String,
        passwordPlain: String,
        displayName: String,
        role: UserRole
    ): AuthResult

    suspend fun login(
        email: String,
        passwordPlain: String,
        twoFactorCode: String? = null
    ): AuthResult

    suspend fun logout(sessionId: String): Boolean

    suspend fun refreshToken(refreshToken: String): AuthResult

    suspend fun requestPasswordReset(email: String): Boolean

    suspend fun verifyEmail(email: String, token: String): Boolean

    suspend fun listActiveSessions(userId: String): List<VyroUserSession>

    suspend fun revokeSession(sessionId: String): Boolean

    suspend fun enableTwoFactor(userId: String, method: String): String // Returns QR/Secret

    suspend fun verifyTwoFactor(userId: String, code: String): Boolean

    suspend fun blockUser(adminUserId: String, targetUserId: String, reason: String): Boolean

    suspend fun suspendAccount(adminUserId: String, targetUserId: String, durationDays: Int, reason: String): Boolean
}
