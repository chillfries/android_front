package com.example.myapplication.data.di

import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.CameraRepositoryImpl
import com.example.myapplication.data.repository.FridgeRepositoryImpl
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.CameraRepository
import com.example.myapplication.domain.repository.FridgeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideCameraRepository(): CameraRepository {
        return CameraRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideFridgeRepository(): FridgeRepository {
        return FridgeRepositoryImpl()
    }
}