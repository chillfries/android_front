package com.example.myapplication.feature.auth.data

// 사용자 정보를 담는 데이터 클래스
data class User(
    val userId: String,          // 사용자 ID (이메일 등)
    val name: String = "",       // 사용자 이름
)