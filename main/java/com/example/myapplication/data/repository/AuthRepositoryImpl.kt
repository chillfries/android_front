package com.example.myapplication.data.repository

import com.example.myapplication.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override fun login(email: String, password: String): Boolean {
        return true
    }

    override fun register(email: String, password: String, confirmPassword: String): Boolean {
        if (password != confirmPassword) {
            return false
        }
        return true
    }
}