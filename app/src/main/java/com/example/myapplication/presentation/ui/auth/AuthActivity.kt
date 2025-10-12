package com.example.myapplication.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import com.example.myapplication.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt 진입점 어노테이션
class AuthActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인증 상태 LiveData 관찰
        authViewModel.isAuthenticated.observe(this) { isAuthenticated ->
            // isAuthenticated가 true일 때만 메인 액티비티로 이동
            if (isAuthenticated == true) {
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // 이전 액티비티 스택을 모두 지우고 새로운 태스크로 시작
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // AuthActivity 종료
    }
}