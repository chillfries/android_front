package com.example.myapplication.data.repository

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.network.AuthApiService
import retrofit2.Response
import javax.inject.Inject
// ✅ DataStore import
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Hilt를 통해 AuthApiService를 주입받도록 @Inject constructor 추가
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val dataStore: DataStore<Preferences> // ✅ DataStore 주입
) : AuthRepository {

    // DataStore에 저장할 키 정의 (로그인 상태)
    private val AUTH_STATUS_KEY = booleanPreferencesKey("is_user_logged_in")

    // 로그인 API 호출 (suspend 함수로 변경)
    override suspend fun login(user: UserLoginRequest): Response<LoginResponse> {
        return apiService.login(user)
    }

    // 회원가입 API 호출 (suspend 함수로 변경)
    override suspend fun register(user: UserCreateRequest): Response<Unit> {
        return apiService.signup(user)
    }

    // 1. 로그인 상태 저장/초기화 구현
    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTH_STATUS_KEY] = isLoggedIn
        }
    }

    // 2. 로그인 상태 Flow 제공 구현
    override fun getAuthStatusFlow(): Flow<Boolean> {
        // DataStore에 저장된 값 (기본값: false)을 반환합니다.
        return dataStore.data
            .map { preferences ->
                preferences[AUTH_STATUS_KEY] ?: false
            }
    }
}