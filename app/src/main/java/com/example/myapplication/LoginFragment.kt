package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 회원가입 화면 이동
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // 2. [핵심] 계속(로그인) 버튼 클릭 로직
        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // 클라이언트 측 유효성 검사
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // **[백엔드 더미 로그인 로직]**
            // TODO: 실제 백엔드 연동 시, 여기에 Retrofit 등의 API 호출 로직을 구현합니다.
            simulateLoginApiCall(email, password)
        }
    }

    // 백엔드 로그인 API 호출을 시뮬레이션하는 더미 함수
    private fun simulateLoginApiCall(email: String, password: String) {
        // API 호출 성공 및 데이터 검증 통과 가정
        Toast.makeText(requireContext(), "로그인 성공 (더미 API 호출 통과)", Toast.LENGTH_SHORT).show()

        // 로그인 성공 시 MainActivity로 전환
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish() // AuthActivity 종료
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}