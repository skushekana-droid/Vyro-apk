package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// VYRO Clean Minimalism Brand Palette
val VyroVioletPrimary = Color(0xFF8B5CF6) // Tailwind violet-500
val VyroVioletDark = Color(0xFF7C3AED)    // Tailwind violet-600
val VyroVioletLight = Color(0xFFA78BFA)   // Tailwind violet-400
val VyroIndigoAccent = Color(0xFF6366F1)  // Tailwind indigo-500

val VyroCyanSecondary = Color(0xFF06B6D4)
val VyroCyanDark = Color(0xFF0891B2)
val VyroCyanLight = Color(0xFF38BDF8)

val VyroGoldTertiary = Color(0xFFF59E0B)
val VyroGoldLight = Color(0xFFFBBF24)

val VyroEmerald = Color(0xFF10B981)
val VyroRose = Color(0xFFF43F5E)

// Clean Minimalism Obsidian Dark Canvas (#050505 / #0A0A0A / #121215)
val VyroBackground = Color(0xFF050505)
val VyroSurface = Color(0xFF0A0A0A)
val VyroSurfaceElevated = Color(0xFF121215)
val VyroSurfaceHighlight = Color(0xFF18181B)
val VyroBorder = Color(0xFF27272A)
val VyroBorderSubtle = Color(0x1AFFFFFF) // Color.White.copy(alpha = 0.08f)

// Slate Clean Minimalism Typography
val VyroTextPrimary = Color(0xFFF8FAFC)   // slate-50
val VyroTextSecondary = Color(0xFFCBD5E1) // slate-300
val VyroTextMuted = Color(0xFF64748B)     // slate-500

// Theme Aliases for Infrastructure Architecture
val VyroObsidianDeep = VyroBackground
val VyroObsidianSurface = VyroSurface
val VyroVioletAccent = VyroVioletPrimary
val VyroZinc200 = Color(0xFFE2E8F0)
val VyroZinc300 = Color(0xFFCBD5E1)
val VyroZinc400 = Color(0xFF94A3B8)
val VyroZinc500 = Color(0xFF64748B)
val VyroZinc700 = Color(0xFF334155)
val VyroZinc800 = Color(0xFF1E293B)

// Gradients
val VyroBrandGradient = Brush.linearGradient(
    listOf(VyroVioletPrimary, VyroIndigoAccent)
)

val VyroEconomyGradient = Brush.horizontalGradient(
    listOf(VyroVioletPrimary, VyroGoldTertiary)
)

val VyroNeonGlowGradient = Brush.verticalGradient(
    listOf(VyroVioletPrimary.copy(alpha = 0.30f), Color.Transparent)
)

