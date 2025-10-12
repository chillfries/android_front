package com.example.myapplication.presentation.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authMessage = MutableLiveData<String>()
    val authMessage: LiveData<String> get() = _authMessage

    private val _isAuthenticated = MutableLiveData<Boolean?>()
    val isAuthenticated: LiveData<Boolean?> get() = _isAuthenticated

    fun performLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authMessage.value = "이메일과 비밀번호를 모두 입력해주세요."
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.login(UserLoginRequest(email, password))
                if (response.isSuccessful) {
                    _authMessage.postValue("로그인 성공!")
                    _isAuthenticated.postValue(true)
                } else {
                    _authMessage.postValue("로그인 실패: ${response.errorBody()?.string()}")
                    _isAuthenticated.postValue(false)
                }
            } catch (e: Exception) {
                _authMessage.postValue("네트워크 오류: ${e.message}")
                _isAuthenticated.postValue(false)
            }
        }
    }

    // RegisterFragment에서 닉네임도 사용하므로 파라미터 추가
    fun performRegister(nickname: String, email: String, password: String, confirmPassword: String) {
        if (nickname.isBlank() || email.isBlank() || password.isBlank()) {
            _authMessage.value = "모든 정보를 입력해주세요."
            return
        }
        if (password != confirmPassword) {
            _authMessage.value = "비밀번호가 일치하지 않습니다."
            return
        }

        viewModelScope.launch {
            try {
                val request = UserCreateRequest(nickname, email, password)
                val response = repository.signup(request)
                if (response.isSuccessful) {
                    _authMessage.postValue("회원가입 성공! 로그인 해주세요.")
                    _isAuthenticated.postValue(null) // 회원가입 성공 시 로그인 화면으로 돌아가야 하므로 null
                } else {
                    _authMessage.postValue("회원가입 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _authMessage.postValue("네트워크 오류: ${e.message}")
            }
        }
    }
}