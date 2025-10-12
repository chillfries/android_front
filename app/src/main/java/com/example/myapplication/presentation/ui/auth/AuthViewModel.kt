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

    private val _isAuthenticated = MutableLiveData<Boolean?>()
    val isAuthenticated: LiveData<Boolean?> get() = _isAuthenticated

    // 회원가입 성공 후 상태를 초기화하기 위한 LiveData
    private val _registrationComplete = MutableLiveData<Boolean>()
    val registrationComplete: LiveData<Boolean> get() = _registrationComplete

    fun performLogin(email: String, password: String) {
        // viewModelScope.launch를 사용해 코루틴 컨텍스트에서 API 호출
        viewModelScope.launch {
            try {
                // UserLoginRequest 객체를 생성하여 전달
                val response = repository.login(UserLoginRequest(email, password))

                // response.isSuccessful로 성공 여부 판단
                if (response.isSuccessful) {
                    _authMessage.postValue(response.body()?.message ?: "로그인 성공!")
                    _isAuthenticated.postValue(true)
                } else {
                    _authMessage.postValue("로그인 실패: ${response.errorBody()?.string()}")
                    _isAuthenticated.postValue(false)
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                _authMessage.postValue("오류가 발생했습니다: ${e.message}")
                _isAuthenticated.postValue(false)
            }
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

    // 회원가입 완료 상태를 리셋하는 함수
    fun onRegistrationCompleteHandled() {
        _registrationComplete.value = false
    }
}