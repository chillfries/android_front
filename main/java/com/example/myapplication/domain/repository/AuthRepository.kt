package com.example.myapplication.domain.repository

import com.example.myapplication.network.LoginResponse
import com.example.myapplication.network.UserCreateRequest
import com.example.myapplication.network.UserLoginRequest
import retrofit2.Response

// 서버 통신을 위한 suspend 함수로 변경
interface AuthRepository {
    suspend fun login(user: UserLoginRequest): Response<LoginResponse>
    suspend fun signup(user: UserCreateRequest): Response<Unit>
}