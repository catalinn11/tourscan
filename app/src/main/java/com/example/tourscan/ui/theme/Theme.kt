package com.example.tourscan.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val customScheme = lightColorScheme(
    primary = Color(0xFF334859),       // App background color
    secondary = Color(0xFF334859),     // Secondary color matching the background
    tertiary = Color(0xFF334859),      // Tertiary color matching the background
    background = Color(0xFF334859),    // Set app background color
    surface = Color(0xFF334859),       // Set surface color to match background
    onPrimary = Color.White,           // White text/icons on primary color
    onSecondary = Color.White,         // White text/icons on secondary color
    onTertiary = Color.White,          // White text/icons on tertiary color
    onBackground = Color.White,        // White text/icons on background
    onSurface = Color.White            // White text/icons on surface
)


@Composable
fun TourScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = customScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}