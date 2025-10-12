package com.example.myapplication.presentation.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

// ⭐ Hilt 어노테이션 추가
@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: AuthViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.authMessage.observe(viewLifecycleOwner) { message ->
            // 메시지가 비어있지 않을 때만 토스트를 띄웁니다.
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 성공(isAuthenticated가 null로 설정됨) 시 로그인 화면으로 돌아가도록 수정
        viewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (isAuthenticated == null) {
                findNavController().popBackStack()
            }
        }

        binding.btnRegister.setOnClickListener {
            // ⭐ 닉네임 값을 가져오도록 수정
            val nickname = binding.etNickname.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // ⭐ performRegister 함수에 nickname을 전달하도록 수정
            viewModel.performRegister(nickname, email, password, confirmPassword)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}