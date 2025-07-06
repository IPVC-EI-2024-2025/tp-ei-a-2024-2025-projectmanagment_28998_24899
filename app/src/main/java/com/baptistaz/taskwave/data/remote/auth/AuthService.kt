package com.baptistaz.taskwave.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * User signup requests.
 *
 * @property email User's email address.
 * @property password User's chosen password.
 */
data class SignupRequest(
    val email: String,
    val password: String
)

/**
 * User login requests.
 *
 * @property email User's email address.
 * @property password User's password.
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response from the Supabase authentication system.
 *
 * @property access_token JWT access token.
 * @property token_type Type of the token ("bearer").
 * @property expires_in Expiration time.
 * @property refresh_token Token used to refresh the session.
 * @property user Basic user information.
 */
data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: SupabaseUser
)

/**
 * Basic user data returned from Supabase after login/signup.
 *
 * @property id Supabase user ID.
 * @property email User's email.
 */
data class SupabaseUser(
    val id: String,
    val email: String
)

/**
 * Authentication endpoints.
 */
interface AuthService {

    /**
     * Signup request to Supabase.
     */
    @POST("signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    /**
     * Login request to Supabase.
     */
    @POST("token?grant_type=password")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
