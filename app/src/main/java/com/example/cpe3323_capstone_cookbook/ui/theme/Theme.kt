package com.example.cpe3323_capstone_cookbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RecipeBookColorScheme = lightColorScheme(
    primary = BurntOrange,
    onPrimary = Color.White,
    primaryContainer = BurntOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = MediumBrown,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = DarkBrown,
    surface = CreamSurface,
    onSurface = DarkBrown,
    surfaceVariant = CardWhite,
    onSurfaceVariant = TextSecondary,
    outline = BorderGrey,
    error = Color(0xFFB00020)
)

@Composable
fun CpE3323_Capstone_CookbookTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RecipeBookColorScheme,
        typography = Typography,
        content = content
    )
}
