package com.example.ui.components

import androidx.compose.ui.graphics.Color

data class AvatarPreset(val emoji: String, val label: String, val color: Color)

val avatarPresets = listOf(
    AvatarPreset("👨", "Papa", Color(0xFFE0F2FE)),
    AvatarPreset("👩", "Mummy", Color(0xFFFEE2E2)),
    AvatarPreset("👶", "Chintu", Color(0xFFF3E8FF)),
    AvatarPreset("👵", "Dadi", Color(0xFFFCE7F3)),
    AvatarPreset("🐹", "🐹", Color(0xFFFEF9C3)),
    AvatarPreset("✨", "Me", Color(0xFFDCFCE7))
)
