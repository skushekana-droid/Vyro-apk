package com.example.infrastructure.payment

import com.example.infrastructure.events.VyroEventBus
import com.example.infrastructure.events.VyroEventType
import java.security.MessageDigest
import java.util.UUID

enum class TransactionStatus {
    PENDING,
    SETTLED,
    REFUNDED,
    FAILED
}

enum class LedgerAccountType {
    USER_WALLET,
    CREATOR_EARNINGS_ESCROW,
    PLATFORM_REVENUE_TREASURY,
    ADVERTISER_ESCROW,
    MEMBERSHIP_POOL
}

data class LedgerEntry(
    val id: String = "tx_${UUID.randomUUID().toString().take(10)}",
    val txHash: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amountGross: Double,
    val platformFee: Double,
    val creatorNet: Double,
    val currency: String = "USD",
    val transactionType: String,
    val status: TransactionStatus = TransactionStatus.SETTLED,
    val description: String,
    val referenceId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class PaymentIntentResult(
    val success: Boolean,
    val clientSecret: String? = null,
    val transactionId: String? = null,
    val errorMessage: String? = null,
    val ledgerEntry: LedgerEntry? = null
)

interface PaymentAdapter {
    val processorName: String
    val isIndependent: Boolean

    suspend fun createPaymentIntent(
        fromUserId: String,
        amount: Double,
        currency: String = "USD",
        metadata: Map<String, String> = emptyMap()
    ): PaymentIntentResult

    suspend fun processPayout(
        creatorId: String,
        amount: Double,
        destinationBankId: String
    ): PaymentIntentResult
}

class VyroPaymentEngine(
    private val paymentAdapter: PaymentAdapter = VyroPayNativeAdapter()
) {
    private val ledger = mutableListOf<LedgerEntry>()

    init {
        // Seed initial immutable ledger transactions
        recordTransaction(
            fromAccountId = "user_viewer1",
            toAccountId = "creator_me",
            amountGross = 50.00,
            feeRate = 0.05,
            type = "CREATOR_SUPER_TIP",
            description = "SuperTip on Deep Dive into Cybernetics"
        )
        recordTransaction(
            fromAccountId = "user_viewer2",
            toAccountId = "creator_me",
            amountGross = 24.99,
            feeRate = 0.10,
            type = "VIP_MEMBERSHIP",
            description = "Monthly VIP Channel Subscription"
        )
    }

    private fun generateCryptographicHash(from: String, to: String, amount: Double, timestamp: Long): String {
        val input = "$from:$to:$amount:$timestamp:vyro_ledger_salt_${UUID.randomUUID()}"
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun recordTransaction(
        fromAccountId: String,
        toAccountId: String,
        amountGross: Double,
        feeRate: Double = 0.05,
        type: String,
        description: String,
        referenceId: String? = null
    ): LedgerEntry {
        val platformFee = amountGross * feeRate
        val creatorNet = amountGross - platformFee
        val timestamp = System.currentTimeMillis()
        val txHash = generateCryptographicHash(fromAccountId, toAccountId, amountGross, timestamp)

        val entry = LedgerEntry(
            txHash = txHash,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amountGross = amountGross,
            platformFee = platformFee,
            creatorNet = creatorNet,
            transactionType = type,
            description = description,
            referenceId = referenceId,
            createdAt = timestamp
        )

        synchronized(ledger) {
            ledger.add(0, entry)
        }

        return entry
    }

    suspend fun processTip(
        fromUserId: String,
        toCreatorId: String,
        amount: Double,
        note: String
    ): PaymentIntentResult {
        if (amount <= 0) return PaymentIntentResult(success = false, errorMessage = "Amount must be positive.")

        val entry = recordTransaction(
            fromAccountId = fromUserId,
            toAccountId = toCreatorId,
            amountGross = amount,
            feeRate = 0.05,
            type = "TIP",
            description = "Tip: $note"
        )

        VyroEventBus.emit(
            VyroEventType.TIP_PROCESSED,
            mapOf("from" to fromUserId, "to" to toCreatorId, "amount" to "$amount", "txHash" to entry.txHash),
            actorId = fromUserId,
            sourceService = "vyro-payment-engine"
        )

        return PaymentIntentResult(
            success = true,
            transactionId = entry.id,
            clientSecret = "pi_${UUID.randomUUID().toString().take(12)}",
            ledgerEntry = entry
        )
    }

    fun getLedgerHistory(limit: Int = 50): List<LedgerEntry> {
        return synchronized(ledger) { ledger.take(limit).toList() }
    }

    fun getCreatorBalance(creatorId: String): Double {
        return synchronized(ledger) {
            ledger.filter { it.toAccountId == creatorId && it.status == TransactionStatus.SETTLED }
                .sumOf { it.creatorNet }
        }
    }
}

class VyroPayNativeAdapter : PaymentAdapter {
    override val processorName: String = "VYRO Direct Settlement Engine (Independent Bank Rail)"
    override val isIndependent: Boolean = true

    override suspend fun createPaymentIntent(fromUserId: String, amount: Double, currency: String, metadata: Map<String, String>): PaymentIntentResult {
        return PaymentIntentResult(
            success = true,
            clientSecret = "vpay_secret_${UUID.randomUUID()}",
            transactionId = "tx_vpay_${UUID.randomUUID().toString().take(8)}"
        )
    }

    override suspend fun processPayout(creatorId: String, amount: Double, destinationBankId: String): PaymentIntentResult {
        return PaymentIntentResult(
            success = true,
            transactionId = "payout_${UUID.randomUUID().toString().take(8)}"
        )
    }
}

class StripePaymentAdapter : PaymentAdapter {
    override val processorName: String = "Stripe Connect Infrastructure Adapter"
    override val isIndependent: Boolean = false

    override suspend fun createPaymentIntent(fromUserId: String, amount: Double, currency: String, metadata: Map<String, String>): PaymentIntentResult {
        return PaymentIntentResult(
            success = true,
            clientSecret = "pi_stripe_${UUID.randomUUID()}",
            transactionId = "tx_stripe_${UUID.randomUUID().toString().take(8)}"
        )
    }

    override suspend fun processPayout(creatorId: String, amount: Double, destinationBankId: String): PaymentIntentResult {
        return PaymentIntentResult(
            success = true,
            transactionId = "po_stripe_${UUID.randomUUID().toString().take(8)}"
        )
    }
}
