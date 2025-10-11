package com.example.myapplication.feature.auth.data

/**
 * 인증 관련 데이터 로직을 처리하는 Repository 클래스입니다.
 * 현재는 테스트 목적으로 어떤 유효한 입력에 대해서도 로그인이 성공하도록 설정되어 있습니다.
 */
class AuthRepository {

    /**
     * 로그인 로직 (무조건 성공 Mock 구현)
     * 어떤 값이 입력되어도 항상 true를 반환하여 로그인 성공 처리.
     */
    fun login(email: String, password: String): Boolean {
        // ⭐ 핵심 수정: 입력값과 상관없이 무조건 true를 반환합니다. ⭐
        return true
    }

    /**
     * 회원가입 로직 (임시 성공 Mock 구현)
     * 비밀번호 확인만 통과하면 성공으로 가정.
     */
    fun register(email: String, password: String, confirmPassword: String): Boolean {
        if (password != confirmPassword) {
            return false
        }
        // TODO: 실제 서버 API 호출 로직을 여기에 구현해야 합니다.
        return true
    }
}