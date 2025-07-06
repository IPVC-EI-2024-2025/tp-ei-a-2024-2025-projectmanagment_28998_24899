package com.baptistaz.taskwave.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.data.remote.auth.AuthRepository

/**
 * Responsible for creating an instance of AuthViewModel
 *
 * @param repository Repository used for authentication operations.
 */
class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    /**
     * Creates the AuthViewModel with the provided repository.
     *
     * @param modelClass The ViewModel class to create.
     * @return A new instance of AuthViewModel.
     * @throws IllegalArgumentException if the ViewModel class is unknown.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
