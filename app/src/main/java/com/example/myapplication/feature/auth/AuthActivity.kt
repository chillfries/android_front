package com.example.myapplication.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.myapplication.MainActivity // 메인 Activity 경로 가정
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    // AuthActivity의 ViewModel 선언은 'by viewModels()' 그대로 유지합니다.
    // Fragment들이 requireActivity()를 통해 이 인스턴스를 공유하게 됩니다.
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⭐ 핵심 관찰 로직: Fragment에서 상태가 true로 변경되면 이 코드가 실행되어야 합니다. ⭐
        authViewModel.isAuthenticated.observe(this, Observer { isAuthenticated ->
            if (isAuthenticated == true) { // Boolean? 타입의 경우 명확히 '== true'로 비교
                navigateToMain()
            }
        })
    }

    /** 로그인 성공 시 MainActivity로 전환하는 시스템 로직 */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // AuthActivity를 종료하여 뒤로 가기로 돌아오지 않도록 합니다.
    }
}