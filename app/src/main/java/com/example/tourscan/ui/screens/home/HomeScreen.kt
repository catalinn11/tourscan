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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // Inject ViewModel
    val viewModel: HomeViewModel = getViewModel()
    val context = LocalContext.current

    // Collect UI State from ViewModel (Analysis status & Result)
    val uiState by viewModel.uiState.collectAsState()

    // Local State for the Image Preview URI
    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    // Temporary URI for Camera capture
    val photoUri = rememberSaveable { mutableStateOf<Uri?>(null) }

    // --- LAUNCHERS ---

    // 1. Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri.value = uri
            // Pass 'false' for isFromCamera -> Do NOT save to DB
            viewModel.onPhotoCaptured(uri.toString(), isFromCamera = false)
        }
    }

    // 2. Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            imageUri.value = photoUri.value
            // Pass 'true' for isFromCamera -> Save to DB
            viewModel.onPhotoCaptured(photoUri.value.toString(), isFromCamera = true)
        }
    }

    // --- PERMISSIONS ---

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

    // --- UI LAYOUT ---

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

                // --- HEADER (Logo) ---
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

                // --- MAIN CONTENT AREA ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Show Intro if no image selected
                    AnimatedVisibility(
                        visible = imageUri.value == null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IntroDisplayContent()
                    }

                    // Show Photo Result if image selected
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
                                PhotoResultCard(
                                    uri = uri,
                                    isAnalyzing = uiState.isAnalyzing,
                                    detectedLabel = uiState.detectedLabel
                                )
                            }
                        }
                    }
                }

                // --- BOTTOM BAR ---
                GlassBottomBar(
                    modifier = Modifier.padding(bottom = 20.dp),
                    onCameraClick = {
                        // Create temp file for camera
                        val newFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                        photoUri.value = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            newFile
                        )
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    },
                    onGalleryClick = {
                        // Handle permissions based on Android version
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

// ================= SUB-COMPONENTS =================

@Composable
fun IntroDisplayContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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
fun PhotoResultCard(
    uri: Uri,
    isAnalyzing: Boolean,
    detectedLabel: String?
) {
    val context = LocalContext.current

    // Gradient for the result border
    val resultBorder = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004494),
            Color(0xFFD4AF37),
            Color(0xFFCE1126)
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Photo Card (Clean, no text overlay)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Inference Result (Displayed BELOW the photo)
        AnimatedVisibility(visible = isAnalyzing || detectedLabel != null) {
            Surface(
                modifier = Modifier
                    // Apply border only if we have a final result
                    .then(
                        if (!isAnalyzing && detectedLabel != null)
                            Modifier.border(1.5.dp, resultBorder, RoundedCornerShape(50))
                        else Modifier
                    ),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyzing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else if (detectedLabel != null) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFFD4AF37) // Gold tint
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // Capitalize the first letter
                        Text(
                            text = detectedLabel.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineSmall, // Bigger text
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
    // Romania Flag Colors (faint for background)
    val romaniaBlurGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004494).copy(alpha = 0.15f),
            Color(0xFFD4AF37).copy(alpha = 0.15f),
            Color(0xFFCE1126).copy(alpha = 0.15f)
        )
    )

    // Romania Flag Colors (more visible for border)
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )

            // Collection Button (Center)
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

            // Divider
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}