package com.example.myapplication.data.model

// 로그인/회원가입 요청 및 응답에 사용할 데이터 클래스들을 정의합니다.
data class UserCreateRequest(val nickname: String, val email: String, val password: String)
data class UserLoginRequest(val email: String, val password: String)
data class LoginResponse(val message: String)