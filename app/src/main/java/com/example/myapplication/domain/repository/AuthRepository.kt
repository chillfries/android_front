package com.example.myapplication.domain.repository

// import 경로를 data.model 패키지로 수정합니다.
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import retrofit2.Response

// 서버 통신을 위한 suspend 함수로 변경
interface AuthRepository {
    suspend fun login(user: UserLoginRequest): Response<LoginResponse>
    suspend fun signup(user: UserCreateRequest): Response<Unit>
}