package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import retrofit2.Response
import kotlinx.coroutines.flow.Flow // Flow import 추가

interface AuthRepository {
    suspend fun login(user: UserLoginRequest): Response<LoginResponse>
    suspend fun register(user: UserCreateRequest): Response<Unit>

    // ✅ 세션 상태 관리 함수 추가
    suspend fun setLoggedIn(isLoggedIn: Boolean) // 로그인 상태 저장/초기화
    fun getAuthStatusFlow(): Flow<Boolean> // 로그인 상태를 Flow로 노출
}