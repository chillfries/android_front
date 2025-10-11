package com.example.myapplication.feature.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.feature.auth.data.AuthRepository // Repository 참조

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authMessage = MutableLiveData<String>()
    val authMessage: LiveData<String> get() = _authMessage

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> get() = _isAuthenticated

    /** 로그인 비즈니스 로직 (수정된 부분) */
    fun performLogin(email: String, password: String) {

        // ⭐ 이전에 있던 입력 유효성 검사 (isBlank) 로직을 제거했습니다. ⭐
        // if (email.isBlank() || password.isBlank()) { ... } 블록 삭제

        // 1. Repository에 인증 로직 위임 (어떤 값이든 넘어갑니다)
        val success = repository.login(email, password)

        // 2. 결과에 따라 LiveData 업데이트 (Activity/Fragment가 관찰)
        if (success) {
            _authMessage.value = "로그인 성공!"
            _isAuthenticated.value = true // AuthActivity가 이 상태를 관찰하여 화면을 전환합니다.
        } else {
            // 이 경로는 AuthRepository가 false를 반환할 때만 실행됩니다. (현재는 무조건 true 예정)
            _authMessage.value = "로그인 실패: 이메일 또는 비밀번호를 확인해주세요."
            _isAuthenticated.value = false
        }
    }

    /** 회원가입 비즈니스 로직 */
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
            _authMessage.value = "회원가입 성공! 로그인 페이지로 이동합니다."
            _isAuthenticated.value = true
        } else {
            _authMessage.value = "회원가입 실패: 다시 시도해주세요."
            _isAuthenticated.value = false
        }
    }
}