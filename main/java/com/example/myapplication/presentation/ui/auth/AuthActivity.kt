package com.example.myapplication.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.myapplication.presentation.MainActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint // ⭐ import 추가

@AndroidEntryPoint // ⭐ Hilt 진입점 어노테이션 추가
class AuthActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.isAuthenticated.observe(this, Observer { isAuthenticated ->
            if (isAuthenticated == true) {
                navigateToMain()
            }
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}