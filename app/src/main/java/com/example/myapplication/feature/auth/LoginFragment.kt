package com.example.myapplication.feature.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding // FragmentLoginBinding 사용
import com.example.myapplication.feature.auth.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    // ⭐ 수정: Fragment 범위(by viewModels()) -> Activity 범위(by viewModels({ requireActivity() })) ⭐
    private val viewModel: AuthViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentLoginBinding? = null
    // View Binding 인스턴스
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

        // 1. LiveData 관찰: 사용자에게 메시지 표시
        viewModel.authMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // ⭐ 2. 로그인 버튼 리스너 (ID: btn_continue) ⭐
        binding.btnContinue.setOnClickListener {
            // ⭐ XML ID에 맞게 수정: et_email, et_password ⭐
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            viewModel.performLogin(email, password) // 비즈니스 로직 위임
        }

        // ⭐ 3. 회원가입 텍스트 리스너 (ID: tv_register) ⭐
        binding.tvRegister.setOnClickListener {
            // nav_auth.xml에 정의된 액션 ID를 사용
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}