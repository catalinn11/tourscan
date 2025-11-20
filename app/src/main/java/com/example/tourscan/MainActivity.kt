package com.example.tourscan

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tourscan.ui.theme.TourScanTheme
import java.io.File

class MainActivity : ComponentActivity() {

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
            val bitmapImage = rememberSaveable { mutableStateOf<Bitmap?>(null) }
            val photoUri = remember { mutableStateOf<Uri?>(null) }

            val galleryLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                    imageUri.value = uri
                    bitmapImage.value = null
                }

            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { isSaved ->
                if (isSaved) {
                    imageUri.value = photoUri.value
                    bitmapImage.value = null
                }
            }

            val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            photoUri.value = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )


            val cameraPermission =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        cameraLauncher.launch(photoUri.value)
                        //cameraLauncher.launch(null)
                    } else {
                        Toast.makeText(context, "Camera permission was not granted", Toast.LENGTH_SHORT).show()
                    }
                }

            val galleryPermission =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        galleryLauncher.launch("image/*")
                    } else {
                        Toast.makeText(context, "Gallery permission was not granted", Toast.LENGTH_SHORT).show()
                    }
                }

            TourScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { it ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        contentAlignment = Alignment.TopCenter
                    ) {

                        if (imageUri.value != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri.value)
                                    .crossfade(true)
                                    //placeholder(R.drawable.placeholder) // Add a placeholder image
                                    //.error(R.drawable.vfkra8jkbuffpdpvtj4okcoabd) // Add an error image
                                    .build(),
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .padding(vertical = 25.dp)
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.5f)
                            )

                        } else if (bitmapImage.value != null) {
                            AsyncImage(
                                model = bitmapImage.value,
                                contentDescription = "Captured Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(vertical = 50.dp)
                                    .fillMaxWidth(0.5f)
                                    .fillMaxHeight(0.5f)
                            )
                        }
                        else {
                            Column(
                                modifier = Modifier.padding(vertical = 25.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.icn),
                                    contentDescription = "No photo",
                                    modifier = Modifier
                                        .size(150.dp),
                                    //contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "TourScan",
                                )
                            }
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {

                            // ────────────────────────────────────────────
                            // Modern Floating Bottom Panel
                            // ────────────────────────────────────────────
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 25.dp)
                                    .fillMaxWidth(0.92f),
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xF20F1113) // semi-transparent modern panel
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 12.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 26.dp, vertical = 14.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Camera Button
                                    Button(
                                        onClick = { cameraPermission.launch(android.Manifest.permission.CAMERA) },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                        modifier = Modifier.size(70.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CameraAlt,
                                            contentDescription = "Camera",
                                            modifier = Modifier.size(32.dp),
                                            tint = Color.White
                                        )
                                    }

                                    // Photo list Button
                                    Button(
                                        onClick = { Toast.makeText(context, "Navigate to photo list screen", Toast.LENGTH_SHORT).show() },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334859)),
                                        modifier = Modifier.size(70.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Photo List",
                                            modifier = Modifier.size(32.dp),
                                            tint = Color.White
                                        )
                                    }

                                    // Gallery Button
                                    Button(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                                galleryPermission.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                                            else
                                                galleryPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                        modifier = Modifier.size(70.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Photo,
                                            contentDescription = "Gallery",
                                            modifier = Modifier.size(32.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                    }
                }

            }
        }
    }
}
