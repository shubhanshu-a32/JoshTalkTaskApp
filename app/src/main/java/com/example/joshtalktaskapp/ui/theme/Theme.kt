package com.example.joshtalktaskapp.ui.theme

import androidx.compose.material.*
import androidx.compose.runtime.Composable

private val LightColors = lightColors(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground
)

@Composable
fun JoshTalkTaskAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColors,
        typography = AppTypography,
        shapes = androidx.compose.material.Shapes(),
        content = content
    )
}
