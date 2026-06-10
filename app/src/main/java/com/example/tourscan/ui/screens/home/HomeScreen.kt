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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.tourscan.ui.language.AppLanguage
import com.example.tourscan.ui.language.LanguageViewModel
import com.example.tourscan.ui.language.LocalAppLanguage
import com.example.tourscan.ui.language.StringKey
import com.example.tourscan.ui.language.Strings
import org.koin.androidx.compose.getViewModel
import java.io.File

@Composable
fun HomeScreen(navController: NavController, paddingValues: PaddingValues) {

    val viewModel: HomeViewModel = getViewModel()
    val context = LocalContext.current
    val languageViewModel: LanguageViewModel = getViewModel(viewModelStoreOwner = context as androidx.activity.ComponentActivity)
    val uiState by viewModel.uiState.collectAsState()
    val lang = LocalAppLanguage.current

    androidx.compose.runtime.LaunchedEffect(lang) {
        viewModel.reloadLandmarkData(lang.code)
    }

    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
    val photoUri = rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri.value = uri
            viewModel.onPhotoCaptured(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            imageUri.value = photoUri.value
            viewModel.onPhotoCaptured(photoUri.value.toString())
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(photoUri.value)
        else Toast.makeText(context, Strings[lang, StringKey.CAMERA_PERMISSION_DENIED], Toast.LENGTH_SHORT).show()
    }

    val galleryPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) galleryLauncher.launch("image/*")
        else Toast.makeText(context, Strings[lang, StringKey.GALLERY_PERMISSION_DENIED], Toast.LENGTH_SHORT).show()
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
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Language toggle button
                    LanguageToggleButton(
                        currentLanguage = lang,
                        onToggle = { languageViewModel.toggleLanguage() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 20.dp, start = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .size(75.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icn),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    ModelSelectorDropdown(
                        selectedModel = uiState.selectedModel,
                        onModelSelected = { viewModel.switchModel(it) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 20.dp, end = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
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
                                PhotoResultCard(
                                    uri = uri,
                                    isAnalyzing = uiState.isAnalyzing,
                                    detectedLabel = uiState.detectedLabel,
                                    landmarkData = uiState.landmarkData
                                )
                            }

                            val isDark = isSystemInDarkTheme()
                            val glassBackground = if (isDark)
                                Color(0xFF2C2C2E).copy(alpha = 0.72f)
                            else
                                Color.White.copy(alpha = 0.78f)
                            val glassBorder = if (isDark)
                                Color.White.copy(alpha = 0.14f)
                            else
                                Color.Black.copy(alpha = 0.07f)
                            val iconColor = if (isDark) Color.White else Color(0xFF1C1C1E)

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(44.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        spotColor = Color.Black.copy(alpha = 0.12f),
                                        ambientColor = Color.Black.copy(alpha = 0.04f)
                                    )
                                    .clip(CircleShape)
                                    .background(glassBackground)
                                    .border(0.6.dp, glassBorder, CircleShape)
                                    .clickable {
                                        context.cacheDir.listFiles()
                                            ?.filter { it.name.startsWith("temp_") }
                                            ?.forEach { it.delete() }
                                        val newFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                                        photoUri.value = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            newFile
                                        )
                                        imageUri.value = null
                                        viewModel.resetPhoto()
                                        cameraPermission.launch(Manifest.permission.CAMERA)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "New Photo",
                                    modifier = Modifier.size(20.dp),
                                    tint = iconColor
                                )
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
    val lang = LocalAppLanguage.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "TourScan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = Strings[lang, StringKey.SUBTITLE],
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
            text = Strings[lang, StringKey.READY_TO_SCAN],
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = Strings[lang, StringKey.TAP_CAMERA],
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoResultCard(
    uri: Uri,
    isAnalyzing: Boolean,
    detectedLabel: String?,
     landmarkData: com.example.tourscan.data.model.LandmarkData?
) {
    val context = LocalContext.current

    val resultBorder = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF002B7F),
            0.28f to Color(0xFF002B7F),
            0.38f to Color(0xFFFCD116),
            0.62f to Color(0xFFFCD116),
            0.72f to Color(0xFFCE1126),
            1.0f to Color(0xFFCE1126)
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedVisibility(visible = true) {
            Surface(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .then(
                        if (!isAnalyzing && detectedLabel != null)
                            Modifier.border(1.5.dp, resultBorder, RoundedCornerShape(50))
                        else Modifier
                    ),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shadowElevation = 0.dp,
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
                            text = Strings[LocalAppLanguage.current, StringKey.ANALYZING],
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else if (detectedLabel != null) {


                        val displayName = landmarkData?.landmarkName
                            ?: detectedLabel.replaceFirstChar { it.uppercase() }
                        //val accuracyText = if (confidence != null) " - ${(confidence * 100).toInt()}%" else ""
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Strings[LocalAppLanguage.current, StringKey.LANDMARK_NOT_RECOGNIZED],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = !isAnalyzing && landmarkData != null) {
            landmarkData?.let { data ->
                LandmarkInfoCarousel(data)
            }
        }
    }
}

sealed class ExpandedState {
    object QuickFacts : ExpandedState()
    object Location : ExpandedState()
    data class Info(val card: com.example.tourscan.data.model.LandmarkCard) : ExpandedState()
}

@ExperimentalFoundationApi
@Composable
fun LandmarkInfoCarousel(data: com.example.tourscan.data.model.LandmarkData) {
    val lang = LocalAppLanguage.current
    var expandedState by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ExpandedState?>(null) }
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.foundation.lazy.LazyRow(
        state = lazyListState,
        flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(bottom = 8.dp)
    ) {
        // Quick Facts Card
        item {
            Card(
                modifier = Modifier.width(280.dp).fillMaxHeight().clickable { expandedState = ExpandedState.QuickFacts },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(Strings[lang, StringKey.QUICK_FACTS], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    data.quickFacts?.forEach { (key, value) ->

                        if (key != "official_website") {

                            Text(
                                text = "• ${formatQuickFactKey(key, lang)}: $value",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    data.quickFacts?.get("official_website")?.let { url ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(Strings[lang, StringKey.OFFICIAL_WEBSITE], style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Location Card
        item {
            Card(
                modifier = Modifier.width(280.dp).fillMaxHeight().clickable { expandedState = ExpandedState.Location },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(Strings[lang, StringKey.LOCATION], style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(data.location, style = MaterialTheme.typography.bodySmall)

                    data.coordinates?.let { coords ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Lat: ${coords.lat}, Lng: ${coords.lng}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    data.googleMapsLink?.let { link ->
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(Strings[lang, StringKey.OPEN_GOOGLE_MAPS], style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Info Cards from JSON
        items(data.cards.size) { index ->
            val card = data.cards[index]
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .clickable { expandedState = ExpandedState.Info(card) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(card.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    rememberScrollState().let { scrollState ->
                        Text(
                            text = card.body,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }
            }
        }
    }

    expandedState?.let { state ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { expandedState = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    when (state) {
                        is ExpandedState.QuickFacts -> {

                            Text(
                                Strings[lang, StringKey.QUICK_FACTS],
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val scrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(scrollState)
                            ) {

                                data.quickFacts?.forEach { (key, value) ->

                                    if (key != "official_website") {

                                        Text(
                                            text = "• ${formatQuickFactKey(key, lang)}: $value",
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }

                                data.quickFacts?.get("official_website")?.let { url ->

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val context = LocalContext.current

                                    OutlinedButton(
                                        onClick = {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                Uri.parse(url)
                                            )
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(Strings[lang, StringKey.OFFICIAL_WEBSITE])
                                    }
                                }
                            }
                        }
                        is ExpandedState.Location -> {
                            Text(Strings[lang, StringKey.LOCATION], style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(data.location, style = MaterialTheme.typography.bodyLarge)

                            data.coordinates?.let { coords ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Lat: ${coords.lat}, Lng: ${coords.lng}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            data.googleMapsLink?.let { link ->
                                Spacer(modifier = Modifier.height(16.dp))
                                val context = LocalContext.current
                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                     Text(Strings[lang, StringKey.OPEN_GOOGLE_MAPS])
                                }
                            }
                        }
                        is ExpandedState.Info -> {
                            val card = state.card
                            Text(card.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            rememberScrollState().let { scrollState ->
                                Text(
                                    text = card.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .verticalScroll(scrollState)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { expandedState = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(Strings[lang, StringKey.CLOSE])
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSelectorDropdown(
    selectedModel: ModelType,
    onModelSelected: (ModelType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier
                .size(48.dp)
                .clickable { expanded = true }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val modelLetter = when (selectedModel) {
                    ModelType.MOBILENET_V2 -> "M"
                    ModelType.EFFICIENT_NET -> "E"
                }
                Text(
                    text = modelLetter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
        ) {
            ModelType.entries.forEach { modelType ->
                val isSelected = selectedModel == modelType
                DropdownMenuItem(
                    text = {
                        Text(
                            text = modelType.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    modifier = Modifier.background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else Color.Transparent
                    ),
                    onClick = {
                        onModelSelected(modelType)
                        expanded = false
                    }
                )
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun formatQuickFactKey(key: String, lang: AppLanguage): String {
    if (lang == AppLanguage.RO) {
        val roTranslations = mapOf(
            "built" to "Construit",
            "architecture_style" to "Stil Arhitectural",
            "recommended_visit_time" to "Timp Recomandat",
            "official_website" to "Site Oficial",
            "feature_type" to "Tip",
            "altitude" to "Altitudine",
            "latitude_note" to "Notă Latitudine",
            "area" to "Suprafață",
            "best_season" to "Cel Mai Bun Sezon",
            "highest_point" to "Cel Mai Înalt Punct",
            "length" to "Lungime",
            "open_season" to "Sezon De Vizitare",
            "depth" to "Adâncime"
        )
        roTranslations[key]?.let { return it }
    }
    
    return key
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                CircleShape
            )
            .clickable(onClick = onToggle)
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = currentLanguage.flag,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
