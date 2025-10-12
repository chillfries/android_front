package com.example.myapplication.data.repository

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.network.AuthApiService
import retrofit2.Response
import javax.inject.Inject

// Hilt를 통해 AuthApiService를 주입받도록 @Inject constructor 추가
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
) : AuthRepository {

    // 로그인 API 호출 (suspend 함수로 변경)
    override suspend fun login(user: UserLoginRequest): Response<LoginResponse> {
        return apiService.login(user)
    }

    // 회원가입 API 호출 (suspend 함수로 변경)
    override suspend fun register(user: UserCreateRequest): Response<Unit> {
        return apiService.signup(user)
    }
}