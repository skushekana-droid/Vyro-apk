package com.example.model

enum class TransactionType {
    TIP_RECEIVED,
    TIP_SENT,
    AD_REVENUE_PAYOUT,
    MEMBERSHIP_SUBSCRIPTION,
    MARKETPLACE_SALE,
    WITHDRAWAL_PROCESSING,
    WITHDRAWAL_COMPLETED
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: String,
    val status: String = "Completed"
)

data class WalletState(
    val availableBalance: Double = 1428.50,
    val pendingEarnings: Double = 380.00,
    val totalLifetimeEarned: Double = 5890.25,
    val transactions: List<WalletTransaction> = emptyList()
)
