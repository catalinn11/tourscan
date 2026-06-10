package com.example.tourscan.di

import androidx.room.Room
import com.example.tourscan.data.local.AppDatabase
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.domain.usecases.SavePhotoUseCase
import com.example.tourscan.domain.usecases.GetLandmarkDataUseCase
import com.example.tourscan.ui.screens.details.PhotoDetailsViewModel
import com.example.tourscan.ui.screens.home.HomeViewModel
import com.example.tourscan.ui.screens.photolist.PhotoListViewModel
import com.example.tourscan.ui.language.LanguageViewModel
import com.google.gson.Gson
import com.example.tourscan.data.security.DatabaseKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { DatabaseKeyManager(androidContext()) }

    // Database & Dao
    single {
        val keyManager = get<DatabaseKeyManager>()
        val passphrase = keyManager.getOrCreatePassphrase()
        val factory = SupportOpenHelperFactory(passphrase.toByteArray())

        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "tourscan.db")
            .openHelperFactory(factory)
            .build()
    }
    single { get<AppDatabase>().photoDao() }

    // JSON Parser
    single { Gson() }

    // Repository
    single { PhotoRepository(get()) }

    // UseCases
    factory { SavePhotoUseCase(androidContext(), get()) }
    factory { GetLandmarkDataUseCase(androidContext(), get()) }

    // ViewModels
    viewModel {
        HomeViewModel(
            app = androidApplication(),
            savePhotoUseCase = get(),
            getLandmarkDataUseCase = get()
        )
    }
    viewModel { PhotoListViewModel(androidApplication(), get()) }
    viewModel { PhotoDetailsViewModel(get(), get(), get()) }
    viewModel { LanguageViewModel(androidApplication()) }
}









