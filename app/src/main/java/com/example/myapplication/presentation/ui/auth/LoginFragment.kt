package com.example.myapplication.presentation.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import com.example.myapplication.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    // ViewModel의 생존 주기를 Activity와 맞추어 Fragment간 데이터 공유
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. LiveData 관찰: 사용자에게 서버 메시지 등을 토스트로 표시
        viewModel.authMessage.observe(viewLifecycleOwner) { message ->
            // 메시지가 비어있지 않을 때만 표시
            if (message.isNotBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // 2. 로그인 버튼 클릭 리스너 (ID: btn_continue)
        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fragment는 ViewModel에 로그인 '요청'만 보낼 뿐, 직접 처리하지 않음
            viewModel.performLogin(email, password)
        }

        // 3. 회원가입 텍스트 클릭 리스너 (ID: tv_register)
        binding.tvRegister.setOnClickListener {
            // nav_auth.xml에 정의된 액션을 사용하여 안전하게 화면 전환
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}