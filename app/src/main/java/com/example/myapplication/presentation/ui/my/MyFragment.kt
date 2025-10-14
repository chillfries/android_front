package com.example.myapplication.presentation.ui.my

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentMyBinding
import com.example.myapplication.presentation.base.BaseFragment
import com.example.myapplication.presentation.ui.auth.AuthActivity
import com.example.myapplication.presentation.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ✅ Hilt 어노테이션 추가
class MyFragment : BaseFragment<FragmentMyBinding>(FragmentMyBinding::inflate) {

    // ✅ AuthViewModel 주입
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ "로그아웃" 텍스트 버튼으로 사용
        binding.tvLogoutButton.setOnClickListener {
            authViewModel.performLogout() // ViewModel에 로그아웃 요청

            // 로그아웃 후 로그인 화면으로 이동
            val intent = Intent(requireActivity(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}