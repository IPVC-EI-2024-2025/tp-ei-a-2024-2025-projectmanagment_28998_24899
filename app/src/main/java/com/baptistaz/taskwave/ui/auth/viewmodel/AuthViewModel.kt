package com.baptistaz.taskwave.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.data.remote.auth.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Handles authentication logic (signup, login, logout).
 *
 * @param repository The authentication repository for calls.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authResponse = MutableStateFlow<Response<AuthResponse>?>(null)

    /** Latest authentication response to the UI layer. */
    val authResponse: LiveData<Response<AuthResponse>?> = _authResponse.asLiveData()

    /**
     * Attempts to sign up the user with the given credentials.
     */
    fun signup(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.signup(email, password)
            _authResponse.value = response
        }
    }

    /**
     * Attempts to log in the user with the given credentials.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.login(email, password)
            _authResponse.value = response
        }
    }

    /**
     * Clears the current authentication response.
     */
    fun clearAuthResponse() {
        _authResponse.value = null
    }
}
