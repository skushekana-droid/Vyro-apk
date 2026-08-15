package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.User
import com.example.ui.theme.*

data class PurchaseItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val date: String,
    val status: String,
    val category: String
)

data class ActiveSubscription(
    val id: String,
    val name: String,
    val tier: String,
    val nextBillingDate: String,
    val priceMonthly: Double,
    val benefits: List<String>
)

@Composable
fun PurchasesScreen(
    currentUser: User,
    onOpenVyroPlusModal: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Subscriptions & Passes", "Order History", "Payment Methods")

    val subscriptions = remember {
        listOf(
            ActiveSubscription(
                id = "sub_1",
                name = "VYRO+ VIP Pass",
                tier = "PRO VIP",
                nextBillingDate = "Sep 14, 2026",
                priceMonthly = 14.99,
                benefits = listOf(
                    "Zero platform ads across all video & short feeds",
                    "4K HDR & 60FPS high-bitrate spatial streaming",
                    "Priority AI Diffusion Lab generation compute",
                    "Golden Creator VIP badge & exclusive emotes"
                )
            ),
            ActiveSubscription(
                id = "sub_2",
                name = "Synthetix Labs Inner Circle",
                tier = "CREATOR TIER 2",
                nextBillingDate = "Sep 02, 2026",
                priceMonthly = 9.99,
                benefits = listOf(
                    "Monthly generative audio pack stems download",
                    "Exclusive behind-the-scenes community guild access",
                    "Live Q&A stream priority question highlighting"
                )
            )
        )
    }

    val purchases = remember {
        listOf(
            PurchaseItem("ord_101", "VYRO Neural Synthesis Kit Vol. 2", "Synthetix Labs Official Store", 19.99, "Aug 12, 2026", "Completed", "Digital Asset"),
            PurchaseItem("ord_102", "Monthly VYRO+ VIP Pass", "VYRO Platform Services", 14.99, "Aug 14, 2026", "Completed", "Subscription"),
            PurchaseItem("ord_103", "Cybernetic Cinema LUTs Bundle", "Kael Orion Storefront", 29.50, "Jul 28, 2026", "Completed", "Digital Asset"),
            PurchaseItem("ord_104", "Elena Vance Live SuperChat Drop", "Elena Vance Official Stream", 20.00, "Jul 15, 2026", "Completed", "Direct Tip")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VyroBackground)
            .padding(horizontal = 16.dp)
            .testTag("purchases_screen"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "COMMERCE & SUBSCRIPTIONS",
                    color = VyroGoldTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Purchases & Memberships",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "Manage your VIP passes, creator subscriptions, and digital order receipts.",
                    color = VyroTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Subtabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VyroSurfaceElevated,
                contentColor = VyroGoldTertiary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) VyroGoldLight else VyroTextMuted,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Subscriptions Tab
                items(subscriptions) { sub ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sub.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${sub.tier} • $${"%.2f".format(sub.priceMonthly)}/month",
                                        color = VyroGoldTertiary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VyroCyanDark.copy(alpha = 0.3f))
                                        .border(1.dp, VyroCyanLight, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("ACTIVE", color = VyroCyanLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(text = "Included Perks:", color = VyroTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            sub.benefits.forEach { benefit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text("✨", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = benefit, color = VyroTextPrimary, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = VyroBorderSubtle)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Renews: ${sub.nextBillingDate}",
                                    color = VyroTextMuted,
                                    fontSize = 11.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onShowSnackbar("Subscription settings updated.") },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VyroTextSecondary)
                                    ) {
                                        Text("Manage", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Upgrade Promo Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VyroBrandGradient)
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "✨ Unlock Creator Economy Superpowers",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get VYRO+ or Creator Pro to claim 95% revenue splits, 4K HDR transcoding, and AI diffusion compute.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenVyroPlusModal,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View VYRO+ Passes", color = VyroVioletPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Order History
                items(purchases) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${item.description} • ${item.date}",
                                    color = VyroTextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VyroSurfaceHighlight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(item.category, color = VyroCyanLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${"%.2f".format(item.price)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = item.status,
                                    color = VyroCyanSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { onShowSnackbar("Receipt for ${item.id} emailed to ${currentUser.username}") },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View Receipt", color = VyroGoldTertiary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Payment Methods
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VyroSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroBorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Linked Payment Methods",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Method 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VyroSurfaceHighlight)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💳", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Visa ending in 8842", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Expires 11/29 • Primary Billing", color = VyroTextMuted, fontSize = 11.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VyroVioletDark)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("DEFAULT", color = VyroVioletLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Method 2: VYRO Wallet
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VyroSurfaceHighlight)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("VYRO Double-Entry Ledger Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Available Balance: $${"%,.2f".format(currentUser.walletBalance)}", color = VyroGoldTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Button(
                                    onClick = { onShowSnackbar("Payment method verified.") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VyroSurfaceElevated)
                                ) {
                                    Text("Use Balance", fontSize = 11.sp, color = VyroCyanLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onShowSnackbar("Add payment modal initialized via Stripe/Ledger adapter.") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VyroVioletPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Payment Method", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
