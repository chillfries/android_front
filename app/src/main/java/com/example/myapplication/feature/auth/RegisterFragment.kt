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
import com.example.myapplication.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {

    // AuthActivity와 동일한 ViewModel 인스턴스를 공유하기 위해 Activity 범위의 viewModels()를 사용합니다.
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

        // 1. LiveData 관찰: 사용자에게 메시지 표시 (토스트 메시지)
        viewModel.authMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 2. LiveData 관찰: 회원가입 성공 시 이전 화면(로그인)으로 돌아가기
        viewModel.isAuthenticated.observe(viewLifecycleOwner) { isRegistered ->
            // 회원가입 성공(true) 시에만 작동
            if (isRegistered == true) {
                // Navigation Component를 사용하여 이전 스택으로 돌아감
                findNavController().popBackStack()
            }
        }

        // ⭐ 3. 회원가입 버튼 클릭 시 ViewModel에 로직 위임 ⭐
        binding.btnRegister.setOnClickListener {

            // UI에서 입력된 값을 가져옵니다.
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // ViewModel의 회원가입 로직 호출
            // 참고: 현재 ViewModel은 닉네임을 사용하지 않으므로 이메일/비밀번호만 넘깁니다.
            viewModel.performRegister(email, password, confirmPassword)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}