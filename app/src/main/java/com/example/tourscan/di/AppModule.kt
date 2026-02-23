package com.example.tourscan.di

import androidx.room.Room
import com.example.tourscan.data.local.AppDatabase
import com.example.tourscan.data.repository.PhotoRepository
import com.example.tourscan.domain.usecases.SavePhotoUseCase
import com.example.tourscan.ui.screens.details.PhotoDetailsViewModel
import com.example.tourscan.ui.screens.home.HomeViewModel
import com.example.tourscan.ui.screens.photolist.PhotoListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database & Dao
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "tourscan_db").build()
    }
    single { get<AppDatabase>().photoDao() }

    // Repository
    single { PhotoRepository(get()) }

    // UseCases
    factory { SavePhotoUseCase(androidContext(), get()) }

    // ViewModels
    viewModel {
        HomeViewModel(
            app = androidApplication(),
            savePhotoUseCase = get()
        )
    }
    viewModel { PhotoListViewModel(get()) }
    viewModel { PhotoDetailsViewModel(get(), get()) }
}








