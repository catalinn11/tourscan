package com.example.tourscan

import android.app.Application
import com.example.tourscan.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TourScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            System.loadLibrary("sqlcipher")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }

        startKoin {
            androidContext(this@TourScanApplication)
            modules(appModule)
        }
    }
}