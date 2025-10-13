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

// Hilt 의존성 주입을 위해 @AndroidEntryPoint 추가
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    // AuthActivity와 동일한 ViewModel 인스턴스를 공유
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

        // 1. authMessage LiveData 관찰: 사용자에게 메시지 표시 (토스트 메시지)
        viewModel.authMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // 2. registrationComplete LiveData 관찰: 회원가입 성공 시 이전 화면(로그인)으로 돌아가기
        viewModel.registrationComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete == true) {
                findNavController().popBackStack()
                // 이벤트가 처리되었음을 ViewModel에 알림
                viewModel.onRegistrationCompleteHandled()
            }
        }

        // 3. 회원가입 버튼 클릭 시 ViewModel에 로직 위임
        binding.btnRegister.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // ViewModel의 회원가입 로직 호출
            viewModel.performRegister(nickname, email, password, confirmPassword)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}