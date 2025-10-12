package com.example.myapplication.domain.repository

interface AuthRepository {
    fun login(email: String, password: String): Boolean
    fun register(email: String, password: String, confirmPassword: String): Boolean
}