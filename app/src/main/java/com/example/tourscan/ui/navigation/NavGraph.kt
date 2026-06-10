package com.example.tourscan.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tourscan.ui.screens.details.PhotoDetailsScreen
import com.example.tourscan.ui.screens.details.PhotoDetailsViewModel
import com.example.tourscan.ui.screens.home.HomeScreen
import com.example.tourscan.ui.screens.photolist.PhotoListScreen
import org.koin.androidx.compose.getViewModel

@Composable
fun NavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, paddingValues = paddingValues)
        }

        composable("photos") {
            PhotoListScreen(navController = navController)
        }

        composable(
            route = "photo_details/{createdAt}",
            arguments = listOf(navArgument("createdAt") { type = NavType.LongType })
        ) {

            val viewModel: PhotoDetailsViewModel = getViewModel()

            PhotoDetailsScreen(viewModel, navController)
        }


    }
}