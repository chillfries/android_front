package com.example.myapplication.presentation.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authMessage = MutableLiveData<String>()
    val authMessage: LiveData<String> get() = _authMessage

    // ⭐ 수정: Boolean을 Nullable Boolean(Boolean?)으로 변경 ⭐
    private val _isAuthenticated = MutableLiveData<Boolean?>()
    val isAuthenticated: LiveData<Boolean?> get() = _isAuthenticated

    fun performLogin(email: String, password: String) {
        val success = repository.login(email, password)
        if (success) {
            _authMessage.value = "로그인 성공!"
            _isAuthenticated.value = true
        } else {
            _authMessage.value = "로그인 실패: 이메일 또는 비밀번호를 확인해주세요."
            _isAuthenticated.value = false
        }
    }

    fun performRegister(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authMessage.value = "모든 정보를 입력해주세요."
            _isAuthenticated.value = false
            return
        }

        if (password != confirmPassword) {
            _authMessage.value = "비밀번호와 비밀번호 확인이 일치하지 않습니다."
            _isAuthenticated.value = false
            return
        }

        val success = repository.register(email, password, confirmPassword)
        if (success) {
            _authMessage.value = "회원가입 성공! 로그인 해주세요."
            // ⭐ 수정: 회원가입 성공 시에는 로그인 상태가 아니므로 null로 설정 ⭐
            _isAuthenticated.value = null
        } else {
            _authMessage.value = "회원가입 실패: 다시 시도해주세요."
            _isAuthenticated.value = false
        }
    }
}