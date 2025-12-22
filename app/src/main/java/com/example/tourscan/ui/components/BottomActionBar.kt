package com.example.tourscan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPhotosClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val cardColor = colors.surfaceColorAtElevation(6.dp) // Works for light/dark mode

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- CAMERA BUTTON (Blue gradient-like) ---
            ModernActionButton(
                icon = Icons.Rounded.CameraAlt,
                tint = Color.White,
                containerColor = Color(0xFF1E88E5),
                onClick = onCameraClick
            )

            // --- PHOTOS BUTTON (Icon only, no background) ---
            IconButtonOnly(
                icon = Icons.AutoMirrored.Filled.List,
                tint = colors.primary,
                onClick = onPhotosClick
            )

            // --- GALLERY BUTTON (Green) ---
            ModernActionButton(
                icon = Icons.Filled.Photo,
                tint = Color.White,
                containerColor = Color(0xFF43A047),
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
fun ModernActionButton(
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(75.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = tint
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        contentPadding = PaddingValues(20.dp) // remove internal padding
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun IconButtonOnly(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(50.dp)
        )
    }
}

