package com.example.myapplication.data.repository

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.network.AuthApiService
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
    // ✅ DataStore 및 CookieJar 의존성 제거
) : AuthRepository {

    override suspend fun login(user: UserLoginRequest): Response<LoginResponse> {
        return apiService.login(user)
    }

    override suspend fun register(user: UserCreateRequest): Response<Unit> {
        return apiService.signup(user)
    }

    override suspend fun logout() {
        // 서버에 로그아웃 API가 있다면 호출합니다.
        // 현재 서버에는 별도 로그아웃 API가 없으므로,
        // 클라이언트 측에서는 아무것도 하지 않아도 앱 재시작 시 쿠키가 초기화됩니다.
    }

    // ✅ getAuthStatusFlow, setLoggedIn 구현부 제거
}