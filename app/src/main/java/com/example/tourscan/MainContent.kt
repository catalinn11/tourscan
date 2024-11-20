package com.example.tourscan

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun MainContent() {

    var permissionGranted by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 75.dp), // Padding to keep buttons above screen's bottom edge
            verticalArrangement = Arrangement.Bottom, // Align buttons at the bottom
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    when {

                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black, // Button background color
                    contentColor = Color.White    // Button text color
                )
            ) {
                Text(
                    text = "Take a Photo",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp, // Increase font size
                )
            }

            Spacer(modifier = Modifier.height(30.dp)) // Space between buttons

            Button(
                onClick = { /* Handle choose from library action */ },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black, // Button background color
                    contentColor = Color.White    // Button text color
                )
            ) {
                Text(
                    text = "Choose from library",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp, // Increase font size
                )
            }
        }
    }
}

fun openCamera(context: Context) {
    // Add your camera opening logic here
    // For example, navigate to another screen with camera functionality
    Toast.makeText(context, "Camera Opened!", Toast.LENGTH_SHORT).show()
}

