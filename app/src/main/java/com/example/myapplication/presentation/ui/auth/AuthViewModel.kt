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
import androidx.lifecycle.asLiveData // ✅ asLiveData import

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authMessage = MutableLiveData<String>()
    val authMessage: LiveData<String> get() = _authMessage

    // ✅ DataStore 상태를 Flow로 받아 LiveData로 노출 (초기 상태 체크 및 UI 업데이트용)
    val isAuthenticated: LiveData<Boolean> = repository.getAuthStatusFlow().asLiveData(viewModelScope.coroutineContext)

    private val _registrationComplete = MutableLiveData<Boolean>()
    val registrationComplete: LiveData<Boolean> get() = _registrationComplete

    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(UserLoginRequest(email, password))

                if (response.isSuccessful) {
                    // 1. DataStore에 로그인 상태 저장
                    repository.setLoggedIn(true) // ✅ DataStore에 true 저장
                    _authMessage.postValue(response.body()?.message ?: "로그인 성공!")
                    // 2. isAuthenticated LiveData가 자동으로 업데이트되므로, 메인 화면으로 이동 처리는 AuthActivity에서 담당합니다.

                } else {
                    repository.setLoggedIn(false) // 로그인 실패 시 상태 초기화
                    _authMessage.postValue("로그인 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                repository.setLoggedIn(false)
                _authMessage.postValue("오류가 발생했습니다: ${e.message}")
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

    // 로그아웃을 위한 함수 (향후 사용)
    fun performLogout() {
        viewModelScope.launch {
            repository.setLoggedIn(false) // DataStore의 로그인 상태를 false로 변경
            // TODO: 서버 로그아웃 API 호출 (현재 없음)
            _authMessage.postValue("로그아웃 되었습니다.")
        }
    }
}