package com.example.memento.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accentSoft: Color,
    val border: Color,
    val green: Color,
    val past: Color,
    val future: Color,
    val futureBorder: Color,
)

val LocalAppColors = staticCompositionLocalOf { darkAppColors() }

fun darkAppColors() = AppColors(
    bg           = Color(0xFF0D0D1A),
    surface      = Color(0xFF16162A),
    surface2     = Color(0xFF1E1E35),
    text         = Color(0xFFE8E8F5),
    muted        = Color(0xFF5A5A80),
    accent       = Color(0xFF7C3AED),
    accentSoft   = Color(0xFFA78BFA),
    border       = Color(0xFF2A2A48),
    green        = Color(0xFF22C55E),
    past         = Color(0xFF3D3D60),
    future       = Color(0xFF181828),
    futureBorder = Color(0xFF222238),
)

fun lightAppColors() = AppColors(
    bg           = Color(0xFFF5F4FF),
    surface      = Color(0xFFFFFFFF),
    surface2     = Color(0xFFF0EFFE),
    text         = Color(0xFF1A1830),
    muted        = Color(0xFF7B7A9A),
    accent       = Color(0xFF7C3AED),
    accentSoft   = Color(0xFF6D28D9),
    border       = Color(0xFFDDDCF5),
    green        = Color(0xFF16A34A),
    past         = Color(0xFFBDBDE0),
    future       = Color(0xFFEEEDFF),
    futureBorder = Color(0xFFDDDCF5),
)
