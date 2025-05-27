package com.baptistaz.taskwave.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SignupRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: SupabaseUser
)

data class SupabaseUser(
    val id: String,
    val email: String
)

interface AuthService {

    @POST("auth/v1/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
