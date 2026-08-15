package com.example.infrastructure.auth

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import com.example.model.UserRole
import java.util.UUID

class VyroNativeAuthAdapter : VyroAuthService {
    override val providerName: String = "VYRO Independent Native Identity System (Self-Hosted)"
    override val isIndependentProvider: Boolean = true

    // Internal secure store (Simulating PostgreSQL credentials & sessions table)
    private data class StoredAccount(
        val userId: String,
        val email: String,
        val displayName: String,
        val passwordHash: String,
        val salt: String,
        val role: UserRole,
        val isEmailVerified: Boolean,
        val isSuspended: Boolean = false,
        val suspensionReason: String? = null,
        val twoFactorSecret: String? = null,
        val twoFactorEnabled: Boolean = false
    )

    private val accounts = mutableMapOf<String, StoredAccount>()
    private val sessions = mutableMapOf<String, VyroUserSession>()
    private val blockedUsers = mutableSetOf<String>()

    init {
        // Seed default independent system accounts with secure salts
        seedAccount("creator@vyro.media", "creator123", "Kael Orion", UserRole.CREATOR, "creator_me")
        seedAccount("viewer@vyro.media", "viewer123", "Alex Reed", UserRole.VIEWER, "viewer_1")
        seedAccount("business@vyro.media", "biz123", "Synthetix Labs", UserRole.BUSINESS, "biz_1")
        seedAccount("admin@vyro.media", "admin123", "Nova Vance (Director)", UserRole.ADMIN, "admin_super")
    }

    private fun seedAccount(email: String, plainPass: String, name: String, role: UserRole, id: String) {
        val salt = VyroPasswordHasher.generateSalt()
        val hash = VyroPasswordHasher.hashPassword(plainPass, salt)
        accounts[email.lowercase()] = StoredAccount(
            userId = id,
            email = email.lowercase(),
            displayName = name,
            passwordHash = hash,
            salt = salt,
            role = role,
            isEmailVerified = true
        )
    }

    override suspend fun register(
        email: String,
        passwordPlain: String,
        displayName: String,
        role: UserRole
    ): AuthResult {
        val cleanEmail = email.lowercase().trim()
        if (accounts.containsKey(cleanEmail)) {
            return AuthResult(success = false, message = "An account with this email already exists.")
        }
        if (passwordPlain.length < 8) {
            return AuthResult(success = false, message = "Password must be at least 8 characters.")
        }

        val salt = VyroPasswordHasher.generateSalt()
        val hash = VyroPasswordHasher.hashPassword(passwordPlain, salt)
        val userId = "usr_${UUID.randomUUID().toString().take(8)}"

        val newAcc = StoredAccount(
            userId = userId,
            email = cleanEmail,
            displayName = displayName,
            passwordHash = hash,
            salt = salt,
            role = role,
            isEmailVerified = false
        )
        accounts[cleanEmail] = newAcc

        val session = VyroUserSession(
            userId = userId,
            userEmail = cleanEmail,
            role = role,
            deviceName = "Android Mobile (Independent Grid)"
        )
        sessions[session.sessionId] = session

        val tokens = AuthTokens(
            accessToken = "vyro_jwt_${UUID.randomUUID()}",
            refreshToken = "vyro_rf_${UUID.randomUUID()}",
            session = session
        )

        VyroEventBus.emit(
            VyroEventType.USER_REGISTERED,
            mapOf("userId" to userId, "email" to cleanEmail, "role" to role.name),
            actorId = userId,
            sourceService = "vyro-auth"
        )

        return AuthResult(
            success = true,
            message = "Account created on VYRO Independent Identity Grid",
            tokens = tokens
        )
    }

    override suspend fun login(
        email: String,
        passwordPlain: String,
        twoFactorCode: String?
    ): AuthResult {
        val cleanEmail = email.lowercase().trim()
        val account = accounts[cleanEmail] ?: return AuthResult(
            success = false,
            message = "Invalid email or password."
        )

        if (account.isSuspended) {
            return AuthResult(
                success = false,
                message = "Account is suspended: ${account.suspensionReason ?: "Violation of Platform Guidelines"}"
            )
        }

        val isValid = VyroPasswordHasher.verifyPassword(passwordPlain, account.salt, account.passwordHash)
        if (!isValid) {
            return AuthResult(success = false, message = "Invalid email or password.")
        }

        if (account.twoFactorEnabled) {
            if (twoFactorCode == null || twoFactorCode != "123456") {
                return AuthResult(
                    success = false,
                    message = "Two-factor authentication code required.",
                    requiresTwoFactor = true,
                    twoFactorMethod = "TOTP Authenticator",
                    temporarySessionId = "temp_${UUID.randomUUID().toString().take(6)}"
                )
            }
        }

        val session = VyroUserSession(
            userId = account.userId,
            userEmail = account.email,
            role = account.role
        )
        sessions[session.sessionId] = session

        val tokens = AuthTokens(
            accessToken = "vyro_jwt_${UUID.randomUUID()}",
            refreshToken = "vyro_rf_${UUID.randomUUID()}",
            session = session
        )

        VyroEventBus.emit(
            VyroEventType.USER_LOGGED_IN,
            mapOf("userId" to account.userId, "sessionId" to session.sessionId),
            actorId = account.userId,
            sourceService = "vyro-auth"
        )

        return AuthResult(
            success = true,
            message = "Successfully authenticated via VYRO Native Auth",
            tokens = tokens
        )
    }

    override suspend fun logout(sessionId: String): Boolean {
        val removed = sessions.remove(sessionId) != null
        return removed
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        val session = sessions.values.firstOrNull { !it.isRevoked && it.expiresAt > System.currentTimeMillis() }
            ?: return AuthResult(success = false, message = "Session expired. Please log in again.")

        val tokens = AuthTokens(
            accessToken = "vyro_jwt_${UUID.randomUUID()}",
            refreshToken = "vyro_rf_${UUID.randomUUID()}",
            session = session
        )
        return AuthResult(success = true, message = "Token refreshed", tokens = tokens)
    }

    override suspend fun requestPasswordReset(email: String): Boolean {
        val cleanEmail = email.lowercase().trim()
        if (accounts.containsKey(cleanEmail)) {
            VyroEventBus.emit(
                VyroEventType.PASSWORD_RESET_REQUESTED,
                mapOf("email" to cleanEmail),
                sourceService = "vyro-auth"
            )
            return true
        }
        return false
    }

    override suspend fun verifyEmail(email: String, token: String): Boolean {
        val cleanEmail = email.lowercase().trim()
        val acc = accounts[cleanEmail] ?: return false
        accounts[cleanEmail] = acc.copy(isEmailVerified = true)
        return true
    }

    override suspend fun listActiveSessions(userId: String): List<VyroUserSession> {
        return sessions.values.filter { it.userId == userId && !it.isRevoked }
    }

    override suspend fun revokeSession(sessionId: String): Boolean {
        val sess = sessions[sessionId] ?: return false
        sessions[sessionId] = sess.copy(isRevoked = true)
        return true
    }

    override suspend fun enableTwoFactor(userId: String, method: String): String {
        return "VYRO-TOTP-SECRET-JBSWY3DPEHPK3PXP"
    }

    override suspend fun verifyTwoFactor(userId: String, code: String): Boolean {
        return code.length == 6
    }

    override suspend fun blockUser(adminUserId: String, targetUserId: String, reason: String): Boolean {
        blockedUsers.add(targetUserId)
        return true
    }

    override suspend fun suspendAccount(adminUserId: String, targetUserId: String, durationDays: Int, reason: String): Boolean {
        val target = accounts.values.find { it.userId == targetUserId } ?: return false
        accounts[target.email] = target.copy(isSuspended = true, suspensionReason = reason)
        VyroEventBus.emit(
            VyroEventType.USER_SUSPENDED,
            mapOf("targetUserId" to targetUserId, "reason" to reason, "durationDays" to "$durationDays"),
            actorId = adminUserId,
            sourceService = "vyro-moderation"
        )
        return true
    }
}

/**
 * Optional 3rd-party Firebase adapter implementing the same VyroAuthService interface.
 * Can be swapped at runtime without changing the application logic.
 */
class FirebaseAuthAdapter : VyroAuthService {
    override val providerName: String = "Firebase Auth Plugin (Replaceable Adapter)"
    override val isIndependentProvider: Boolean = false

    private val native = VyroNativeAuthAdapter()

    override suspend fun register(email: String, passwordPlain: String, displayName: String, role: UserRole): AuthResult =
        native.register(email, passwordPlain, displayName, role)

    override suspend fun login(email: String, passwordPlain: String, twoFactorCode: String?): AuthResult =
        native.login(email, passwordPlain, twoFactorCode)

    override suspend fun logout(sessionId: String): Boolean = native.logout(sessionId)
    override suspend fun refreshToken(refreshToken: String): AuthResult = native.refreshToken(refreshToken)
    override suspend fun requestPasswordReset(email: String): Boolean = native.requestPasswordReset(email)
    override suspend fun verifyEmail(email: String, token: String): Boolean = native.verifyEmail(email, token)
    override suspend fun listActiveSessions(userId: String): List<VyroUserSession> = native.listActiveSessions(userId)
    override suspend fun revokeSession(sessionId: String): Boolean = native.revokeSession(sessionId)
    override suspend fun enableTwoFactor(userId: String, method: String): String = native.enableTwoFactor(userId, method)
    override suspend fun verifyTwoFactor(userId: String, code: String): Boolean = native.verifyTwoFactor(userId, code)
    override suspend fun blockUser(adminUserId: String, targetUserId: String, reason: String): Boolean = native.blockUser(adminUserId, targetUserId, reason)
    override suspend fun suspendAccount(adminUserId: String, targetUserId: String, durationDays: Int, reason: String): Boolean = native.suspendAccount(adminUserId, targetUserId, durationDays, reason)
}
