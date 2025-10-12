package com.example.myapplication.network

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/v1/users/signup")
    suspend fun signup(@Body user: UserCreateRequest): Response<Unit>

    @POST("/api/v1/users/login")
    suspend fun login(@Body user: UserLoginRequest): Response<LoginResponse>
}