package com.baptistaz.taskwave.data.remote

import User
import android.util.Log

class UserRepository {

    suspend fun getAllUsers(token: String): List<User>? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getAllUsers()
        Log.d("USER_REPO", "getAllUsers - Response code: ${response.code()}")
        Log.d("USER_REPO", "getAllUsers - Response body: ${response.body()}")
        if (!response.isSuccessful) {
            Log.e("USER_REPO_ERROR", "getAllUsers - Error: ${response.errorBody()?.string()}")
        }
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getUserByAuthId(authId: String, token: String): User? {
        val apiService = RetrofitInstance.getApiService(token)
        Log.d("USER_REPO", "getUserByAuthId - authId: $authId")
        val response = apiService.getUserByAuthId("eq.$authId")
        Log.d("USER_REPO", "getUserByAuthId - Response code: ${response.code()}")
        Log.d("USER_REPO", "getUserByAuthId - Response body: ${response.body()}")
        if (!response.isSuccessful) {
            Log.e("USER_REPO_ERROR", "getUserByAuthId - Error: ${response.errorBody()?.string()}")
        }
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }

    suspend fun createUser(user: User, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        Log.d("USER_REPO", "createUser - User: $user")
        val response = apiService.createUser(user)
        Log.d("USER_REPO", "createUser - Response code: ${response.code()}")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("USER_REPO_ERROR", "createUser - Error: ${response.code()} - $errorBody")
        }
        return response.isSuccessful
    }
}