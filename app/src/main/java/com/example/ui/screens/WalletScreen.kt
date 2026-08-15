package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionType
import com.example.model.WalletState
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    walletState: WalletState,
    onRequestPayout: () -> Unit
) {
    var showPayoutSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("wallet_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "VYRO CONTENT ECONOMY",
                    color = VyroGoldTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Creator Wallet & Earnings",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }
        }

        // Balance Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(VyroBrandGradient)
                    .padding(22.dp)
            ) {
                Text(
                    text = "AVAILABLE BALANCE",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${"%,.2f".format(walletState.availableBalance)}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 34.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Pending Settlement", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(
                            text = "$${"%,.2f".format(walletState.pendingEarnings)}",
                            color = VyroGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            onRequestPayout()
                            showPayoutSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("request_payout_btn")
                    ) {
                        Text("Withdraw Funds", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (showPayoutSuccess) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VyroEmerald.copy(alpha = 0.2f))
                        .border(1.dp, VyroEmerald, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VyroEmerald)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Payout request submitted! Transfer will arrive in 24-48h.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Lifetime Metric Highlights
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(text = "Total Lifetime Earned", color = VyroTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${"%,.2f".format(walletState.totalLifetimeEarned)}",
                        color = VyroGoldTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VyroSurfaceElevated)
                        .border(1.dp, VyroBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(text = "Monetization Split", color = VyroTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "85% Creator / 15% Net",
                        color = VyroCyanLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Transaction Ledger
        item {
            Text(
                text = "Transaction History",
                color = VyroTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        items(walletState.transactions) { tx ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroSurfaceElevated)
                    .border(1.dp, VyroBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (tx.type) {
                        TransactionType.TIP_RECEIVED -> "⚡"
                        TransactionType.AD_REVENUE_PAYOUT -> "📺"
                        TransactionType.MARKETPLACE_SALE -> "🛍️"
                        TransactionType.TIP_SENT -> "💸"
                        TransactionType.MEMBERSHIP_SUBSCRIPTION -> "⭐"
                        TransactionType.WITHDRAWAL_PROCESSING -> "⏳"
                        TransactionType.WITHDRAWAL_COMPLETED -> "🏦"
                    }
                    Text(text = icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tx.title,
                            color = VyroTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${tx.subtitle} • ${tx.timestamp}",
                            color = VyroTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                val isPositive = tx.amount > 0
                Text(
                    text = "${if (isPositive) "+" else ""}$${"%,.2f".format(tx.amount)}",
                    color = if (isPositive) VyroEmerald else VyroRose,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
