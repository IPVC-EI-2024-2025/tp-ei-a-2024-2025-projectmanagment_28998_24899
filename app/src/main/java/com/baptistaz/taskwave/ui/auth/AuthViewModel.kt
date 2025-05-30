package com.baptistaz.taskwave.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.data.remote.auth.AuthResponse
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authResponse = MutableStateFlow<Response<AuthResponse>?>(null)
    val authResponse: StateFlow<Response<AuthResponse>?> = _authResponse

    fun signup(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.signup(email, password)
            _authResponse.value = response
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.login(email, password)
            _authResponse.value = response
        }
    }

    fun logout(context: android.content.Context) {
        SessionManager.clearAccessToken(context)
        _authResponse.value = null
    }

    fun clearAuthResponse() {
        _authResponse.value = null
    }
}
