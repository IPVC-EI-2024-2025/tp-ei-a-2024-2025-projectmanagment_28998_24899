package com.baptistaz.taskwave.data.remote.auth

import retrofit2.Response

/**
 * Communicates with the AuthService to perform signup and login actions.
 *
 * @property service Retrofit service that defines the API endpoints.
 */
class AuthRepository(private val service: AuthService) {

    /**
     * Registers a new user with the given email and password.
     *
     * @param email User's email.
     * @param password User's password.
     * @return Response containing authentication data or error.
     */
    suspend fun signup(email: String, password: String): Response<AuthResponse> {
        val request = SignupRequest(email, password)
        return service.signup(request)
    }

    /**
     * Log-in an existing user with the given email and password.
     *
     * @param email User's email.
     * @param password User's password.
     * @return Response containing authentication data or error.
     */
    suspend fun login(email: String, password: String): Response<AuthResponse> {
        val request = LoginRequest(email, password)
        return service.login(request)
    }
}
