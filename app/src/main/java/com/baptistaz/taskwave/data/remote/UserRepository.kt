package com.baptistaz.taskwave.data.remote

import User
import android.util.Log

class UserRepository {

    suspend fun getAllUsers(token: String): List<User>? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getAllUsers()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getUserByAuthId(authId: String, token: String): User? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUserByAuthId("eq.$authId")
        Log.d("DEBUG", "response code: ${response.code()}")
        Log.d("DEBUG", "response body: ${response.body()}")
        Log.d("DEBUG", "response error: ${response.errorBody()?.string()}")
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }

    suspend fun createUser(user: User, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.createUser(user)
        return response.isSuccessful
    }
}