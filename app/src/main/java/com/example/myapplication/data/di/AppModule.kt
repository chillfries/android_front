package com.example.myapplication.data.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.FridgeDao
import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.CameraRepositoryImpl
import com.example.myapplication.data.repository.FridgeRepositoryImpl
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.CameraRepository
import com.example.myapplication.domain.repository.FridgeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Database & DAO ---
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_fridge_app.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFridgeDao(appDatabase: AppDatabase): FridgeDao {
        return appDatabase.fridgeDao()
    }

    // --- Repositories ---
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

    // FridgeRepositoryImpl을 FridgeRepository의 구현체로 제공한다고 Hilt에 명시합니다.
    @Provides
    @Singleton
    fun provideFridgeRepository(fridgeDao: FridgeDao): FridgeRepository {
        return FridgeRepositoryImpl(fridgeDao)
    }
}