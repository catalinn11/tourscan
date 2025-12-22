package com.example.tourscan.ui.screens.home

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tourscan.R
import org.koin.androidx.compose.getViewModel
import java.io.File

@Composable
fun HomeScreen(navController: NavController, paddingValues: PaddingValues) {

    val viewModel: HomeViewModel = getViewModel()
    val context = LocalContext.current

    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoUri = rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri.value = uri
            viewModel.savePhoto(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            imageUri.value = photoUri.value
            viewModel.savePhoto(photoUri.value.toString())
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(photoUri.value)
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    val galleryPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) galleryLauncher.launch("image/*")
        else Toast.makeText(context, "Gallery permission denied", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icn),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(60.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = imageUri.value == null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IntroDisplayContent()
                    }

                    AnimatedVisibility(
                        visible = imageUri.value != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            imageUri.value?.let { uri ->
                                PhotoResultCard(uri)
                            }
                        }
                    }
                }

                GlassBottomBar(
                    modifier = Modifier.padding(bottom = 20.dp),
                    onCameraClick = {
                        val newFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                        photoUri.value = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            newFile
                        )
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    },
                    onGalleryClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            galleryPermission.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        else
                            galleryPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    },
                    onPhotosClick = {
                        navController.navigate("photos")
                    }
                )
            }
        }
    }
}

@Composable
fun IntroDisplayContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TourScan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Discover the places of Romania",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Filled.ImageSearch,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )

        Text(
            text = "Ready to Scan?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "Tap the camera below to start.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PhotoResultCard(uri: Uri) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyzing...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun GlassBottomBar(
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onPhotosClick: () -> Unit
) {
    val romaniaBlurGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004494).copy(alpha = 0.15f),
            Color(0xFFD4AF37).copy(alpha = 0.15f),
            Color(0xFFCE1126).copy(alpha = 0.15f)
        )
    )

    val borderGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004494).copy(alpha = 0.3f),
            Color(0xFFD4AF37).copy(alpha = 0.3f),
            Color(0xFFCE1126).copy(alpha = 0.3f)
        )
    )

    Box(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .height(80.dp)
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(50.dp),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .background(romaniaBlurGradient)
            .border(width = 1.dp, brush = borderGradient, shape = RoundedCornerShape(50.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCameraClick, modifier = Modifier.size(50.dp)) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Camera",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            IconButton(
                onClick = onPhotosClick,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "List",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            IconButton(onClick = onGalleryClick, modifier = Modifier.size(50.dp)) {
                Icon(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = "Gallery",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}