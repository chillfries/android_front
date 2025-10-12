package com.example.myapplication.data.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.myapplication.BuildConfig // BuildConfig를 import 합니다.
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.FridgeDao
import com.example.myapplication.data.repository.AuthRepositoryImpl
import com.example.myapplication.data.repository.CameraRepositoryImpl
import com.example.myapplication.data.repository.FridgeRepositoryImpl
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.repository.CameraRepository
import com.example.myapplication.domain.repository.FridgeRepository
import com.example.myapplication.network.AuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Network Dependencies ---
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API_LOG", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // build.gradle.kts에 추가한 URL 사용
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }


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
    // (기존 Repository Provider들은 수정 없이 그대로 유지)
    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApiService): AuthRepository {
        // AuthRepositoryImpl이 AuthApiService를 필요로 하므로, 파라미터로 받아 전달합니다.
        return AuthRepositoryImpl(authApi)
    }

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
    fun provideFridgeRepository(fridgeDao: FridgeDao): FridgeRepository {
        return FridgeRepositoryImpl(fridgeDao)
    }
}