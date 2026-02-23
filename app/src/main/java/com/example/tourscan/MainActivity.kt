package com.example.tourscan

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.tourscan.di.appModule
import com.example.tourscan.ui.navigation.NavGraph
import com.example.tourscan.ui.theme.TourScanTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        startKoin {
//            androidContext(this@MainActivity)
//            modules(appModule)
//        }
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            TourScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavGraph(navController = navController, paddingValues = padding)
                }
            }
        }
    }
}
