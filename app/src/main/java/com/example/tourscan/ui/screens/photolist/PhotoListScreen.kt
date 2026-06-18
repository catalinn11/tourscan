package com.example.tourscan.ui.screens.photolist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.tourscan.ui.language.LocalAppLanguage
import com.example.tourscan.ui.language.StringKey
import com.example.tourscan.ui.language.Strings
import com.example.tourscan.utils.EmptyState
import com.example.tourscan.utils.LoadingShimmer
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(navController: NavController) {

    val viewModel: PhotoListViewModel = getViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val lang = LocalAppLanguage.current

    // Confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(Strings[lang, StringKey.DELETE_ALL_PHOTOS]) },
            text = { Text(Strings[lang, StringKey.DELETE_ALL_CONFIRM]) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllPhotos()
                    showDeleteDialog = false
                }) {
                    Text(Strings[lang, StringKey.DELETE], color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(Strings[lang, StringKey.CANCEL])
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(Strings[lang, StringKey.PHOTO_LIST]) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.photos.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = Strings[lang, StringKey.DELETE_ALL_PHOTOS],
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        when {
            uiState.loading || (!uiState.imagesReady && uiState.photos.isNotEmpty()) -> {
                LoadingShimmer(modifier = Modifier.padding(padding))
            }

            uiState.photos.isEmpty() -> {
                EmptyState(modifier = Modifier.padding(padding))
            }

            else -> {
                PhotoGrid(
                    photos = uiState.photos,
                    onClick = { createdAt ->
                        navController.navigate("photo_details/$createdAt")
                    },
                    onDelete = { createdAt ->
                        viewModel.deletePhoto(createdAt)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

