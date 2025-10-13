package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import retrofit2.Response
import kotlinx.coroutines.flow.Flow // Flow import 추가

interface AuthRepository {
    suspend fun login(user: UserLoginRequest): Response<LoginResponse>
    suspend fun register(user: UserCreateRequest): Response<Unit>
    suspend fun logout()
}