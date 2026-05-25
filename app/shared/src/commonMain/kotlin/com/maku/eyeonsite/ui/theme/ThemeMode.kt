package com.maku.eyeonsite.ui.theme

enum class ThemeMode(
    val label: String,
    val description: String,
) {
    SYSTEM("System", "Follows your device setting"),
    LIGHT("Light", "Always light"),
    DARK("Dark", "Always dark"),
}
