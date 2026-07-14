package com.linkit.company.domain.repository

import com.linkit.company.domain.model.auth.Auth

interface AuthRepository {

    /**
     * 기기 Device ID로 로그인(기존 회원) 또는 회원가입(신규 회원)을 통합 처리한다.
     *
     * 별도 로그인 화면 없이 앱 설치 후 최초 확보한 Device ID를 계정 식별자로 사용하며,
     * 성공 시 accessToken 영구 저장까지 책임진다.
     */
    suspend fun login(): Auth

    suspend fun isLoggedIn(): Boolean

    suspend fun logout()
}
