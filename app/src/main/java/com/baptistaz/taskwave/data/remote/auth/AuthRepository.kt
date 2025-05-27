package com.baptistaz.taskwave.data.remote.auth

import retrofit2.Response

class AuthRepository(private val service: AuthService) {

    suspend fun signup(email: String, password: String): Response<AuthResponse> {
        val request = SignupRequest(email, password)
        return service.signup(request)
    }

    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(email, password)
        return service.login(request)
    }
}
