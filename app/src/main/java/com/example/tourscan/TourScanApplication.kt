package com.example.tourscan

import android.app.Application
import com.example.tourscan.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TourScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize SQLCipher native libraries
        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            // Fallback or log if library is not found
            e.printStackTrace()
        }

        startKoin {
            // Pass the Application Context here
            androidContext(this@TourScanApplication)
            modules(appModule)
        }
    }
}