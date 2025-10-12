package com.example.myapplication.data.repository

import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.network.AuthApiService
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import retrofit2.Response
import javax.inject.Inject

// Hilt를 통해 AuthApiService를 주입받습니다.
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService
) : AuthRepository {

    override suspend fun login(user: UserLoginRequest): Response<LoginResponse> {
        return authApi.login(user)
    }

    override suspend fun signup(user: UserCreateRequest): Response<Unit> {
        return authApi.signup(user)
    }
}