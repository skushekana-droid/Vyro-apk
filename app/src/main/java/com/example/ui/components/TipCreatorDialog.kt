package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import com.example.model.Video
import com.example.ui.theme.*

@Composable
fun TipCreatorDialog(
    video: Video,
    userWalletBalance: Double,
    onDismiss: () -> Unit,
    onSendTip: (amount: Double, note: String) -> Unit
) {
    val presetAmounts = listOf(2.0, 5.0, 10.0, 25.0, 50.0)
    var selectedAmount by remember { mutableStateOf(5.0) }
    var customAmountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, VyroBorder, RoundedCornerShape(24.dp)),
            color = VyroSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Support Creator",
                                color = VyroTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = video.creatorName,
                                color = VyroCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VyroTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Tip Amount",
                    color = VyroTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetAmounts.forEach { amount ->
                        val isSelected = selectedAmount == amount && customAmountText.isEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VyroGoldTertiary else VyroSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) VyroGoldLight else VyroBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedAmount = amount
                                    customAmountText = ""
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$${amount.toInt()}",
                                color = if (isSelected) Color.Black else VyroTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom amount field
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = {
                        customAmountText = it
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            selectedAmount = parsed
                        }
                    },
                    label = { Text("Custom Amount ($)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroGoldTertiary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Note message
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Add an encouraging message (optional)") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroVioletPrimary,
                        unfocusedBorderColor = VyroBorder,
                        focusedTextColor = VyroTextPrimary,
                        unfocusedTextColor = VyroTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Wallet Balance Note
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VyroSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Your Wallet Balance:", color = VyroTextSecondary, fontSize = 12.sp)
                    Text(
                        text = "$${"%,.2f".format(userWalletBalance)}",
                        color = VyroGoldTertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button
                Button(
                    onClick = {
                        val finalAmount = customAmountText.toDoubleOrNull() ?: selectedAmount
                        onSendTip(finalAmount, noteText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_send_tip_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroGoldTertiary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Send $${"%.2f".format(if (customAmountText.isNotEmpty()) (customAmountText.toDoubleOrNull() ?: selectedAmount) else selectedAmount)} Tip ⚡",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
