package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // **[핵심]** 초기 인증 상태 확인 로직을 제거합니다.
        // 앱은 항상 AuthActivity로 시작하여 로그인 화면(LoginFragment)을 띄웁니다.
    }
}