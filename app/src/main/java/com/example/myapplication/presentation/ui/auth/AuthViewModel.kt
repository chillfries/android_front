package com.example.myapplication.presentation.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.UserCreateRequest
import com.example.myapplication.data.model.UserLoginRequest
import com.example.myapplication.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authMessage = MutableLiveData<String>()
    val authMessage: LiveData<String> get() = _authMessage

    // ✅ 자동 로그인 관련 LiveData 제거
    // val isAuthenticated: LiveData<Boolean> = repository.getAuthStatusFlow().asLiveData(viewModelScope.coroutineContext)

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _registrationComplete = MutableLiveData<Boolean>()
    val registrationComplete: LiveData<Boolean> get() = _registrationComplete

    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(UserLoginRequest(email, password))
                if (response.isSuccessful) {
                    _authMessage.postValue(response.body()?.message ?: "로그인 성공!")
                    _loginSuccess.postValue(true) // ✅ 로그인 성공 시 이벤트 발생
                } else {
                    _authMessage.postValue("로그인 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _authMessage.postValue("오류가 발생했습니다: ${e.message}")
            }
        }
    }

    fun onLoginSuccessHandled() {
        _loginSuccess.value = false
    }

    // ... (performRegister, onRegistrationCompleteHandled 함수는 동일)

    fun performLogout() {
        viewModelScope.launch {
            repository.logout()
            _authMessage.postValue("로그아웃 되었습니다.")
        }
    }

    fun performRegister(nickname: String, email: String, password: String, confirmPassword: String) {
        if (nickname.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authMessage.value = "모든 정보를 입력해주세요."
            return
        }

        if (password != confirmPassword) {
            _authMessage.value = "비밀번호가 일치하지 않습니다."
            return
        }

        viewModelScope.launch {
            try {
                // UserCreateRequest 객체를 생성하여 전달
                val request = UserCreateRequest(nickname = nickname, email = email, password = password)
                val response = repository.register(request)

                if (response.isSuccessful) {
                    _authMessage.postValue("회원가입 성공! 로그인 해주세요.")
                    _registrationComplete.postValue(true)
                } else {
                    _authMessage.postValue("회원가입 실패: ${response.errorBody()?.string() ?: "알 수 없는 오류"}")
                }
            } catch (e: Exception) {
                _authMessage.postValue("오류가 발생했습니다: ${e.message}")
            }
        }
    }

    fun onRegistrationCompleteHandled() {
        _registrationComplete.value = false
    }
}