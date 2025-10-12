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

// ✅ DataStore import 추가
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import okhttp3.JavaNetCookieJar // ✅ CookieJar import
import java.net.CookieManager // ✅ CookieManager import

// DataStore 인스턴스 생성 정의
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

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

        // ✅ CookieJar 추가: 서버가 보내는 세션 쿠키를 자동으로 저장합니다.
        val cookieManager = CookieManager()
        val cookieJar = JavaNetCookieJar(cookieManager)

        return OkHttpClient.Builder()
            .cookieJar(cookieJar) // ✅ CookieJar 적용
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

    // 1. DataStore Provider 추가
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
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
    @Provides
    @Singleton
    fun provideAuthRepository(apiService: AuthApiService, dataStore: DataStore<Preferences>): AuthRepository { // ✅ DataStore 인자 추가
        return AuthRepositoryImpl(apiService, dataStore) // ✅ DataStore 전달
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