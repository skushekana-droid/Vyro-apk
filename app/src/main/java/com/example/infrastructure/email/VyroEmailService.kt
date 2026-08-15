package com.example.infrastructure.email

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType

data class EmailMessage(
    val to: String,
    val subject: String,
    val htmlBody: String,
    val templateId: String
)

interface EmailService {
    val providerName: String
    val isIndependent: Boolean

    suspend fun sendVerificationEmail(email: String, verificationToken: String): Boolean
    suspend fun sendPasswordResetEmail(email: String, resetLink: String): Boolean
    suspend fun sendTipNotificationEmail(creatorEmail: String, tipperName: String, amount: Double): Boolean
    suspend fun sendSecurityAlert(email: String, alertDetails: String): Boolean
}

class VyroNativeEmailAdapter : EmailService {
    override val providerName: String = "VYRO Self-Hosted Postfix / SES SMTP Gateway"
    override val isIndependent: Boolean = true

    override suspend fun sendVerificationEmail(email: String, verificationToken: String): Boolean {
        VyroEventBus.emit(
            VyroEventType.SECURITY_ALERT,
            mapOf("type" to "EMAIL_SENT", "to" to email, "template" to "EMAIL_VERIFICATION"),
            sourceService = "vyro-email"
        )
        return true
    }

    override suspend fun sendPasswordResetEmail(email: String, resetLink: String): Boolean = true
    override suspend fun sendTipNotificationEmail(creatorEmail: String, tipperName: String, amount: Double): Boolean = true
    override suspend fun sendSecurityAlert(email: String, alertDetails: String): Boolean = true
}
