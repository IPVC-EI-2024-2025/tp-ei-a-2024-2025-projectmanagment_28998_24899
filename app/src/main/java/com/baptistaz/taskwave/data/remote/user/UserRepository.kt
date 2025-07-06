package com.baptistaz.taskwave.data.remote.user

import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.auth.UserUpdate
import android.util.Log
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance

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
        Log.d("USER_REPO", "createUser - com.baptistaz.taskwave.data.model.auth.User: $user")
        val response = apiService.createUser(user)
        Log.d("USER_REPO", "createUser - Response code: ${response.code()}")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("USER_REPO_ERROR", "createUser - Error: ${response.code()} - $errorBody")
        }
        return response.isSuccessful
    }

    suspend fun getUserById(id: String, token: String): User? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUserById("eq.$id")
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }

    suspend fun updateUser(userId: String, userUpdate: UserUpdate, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.updateUser("eq.$userId", userUpdate)
        if (!response.isSuccessful) {
            Log.e("EDIT_USER", "Erro ao atualizar user: ${response.code()} - ${response.errorBody()?.string()}")
        }
        return response.isSuccessful
    }

    suspend fun deleteUser(userId: String, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.deleteUser("eq.$userId")
        if (!response.isSuccessful) {
            Log.e("EDIT_USER", "Erro ao eliminar user: ${response.code()} - ${response.errorBody()?.string()}")
        }
        return response.isSuccessful
    }

    suspend fun getAllManagers(token: String): List<User>? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUsersByProfileType("eq.GESTOR")
        Log.d("USER_REPO", "getAllManagers - Response code: ${response.code()}")
        Log.d("USER_REPO", "getAllManagers - Response body: ${response.body()}")
        if (!response.isSuccessful) {
            Log.e("USER_REPO_ERROR", "getAllManagers - Error: ${response.errorBody()?.string()}")
        }
        return if (response.isSuccessful) response.body() else null
    }

}