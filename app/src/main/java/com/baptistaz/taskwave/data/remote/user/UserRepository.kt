package com.baptistaz.taskwave.data.remote.user

import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.auth.UserUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance

/**
 * Manages user-related operations via ApiService.
 */
class UserRepository {

    /**
     * All users.
     *
     * @param token JWT token for authentication.
     * @return List of users.
     */
    suspend fun getAllUsers(token: String): List<User>? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getAllUsers()
        return if (response.isSuccessful) response.body() else null
    }

    /**
     * User by their Supabase auth ID.
     *
     * @param authId Supabase auth ID.
     * @param token JWT token.
     * @return User object or null.
     */
    suspend fun getUserByAuthId(authId: String, token: String): User? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUserByAuthId("eq.$authId")
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }

    /**
     * Creates a new user.
     *
     * @param user User object to create.
     * @param token JWT token.
     * @return True if created successfully, false if not.
     */
    suspend fun createUser(user: User, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.createUser(user)
        return response.isSuccessful
    }

    /**
     * User by internal ID.
     *
     * @param id User ID.
     * @param token JWT token.
     * @return User object or null.
     */
    suspend fun getUserById(id: String, token: String): User? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUserById("eq.$id")
        return if (response.isSuccessful) response.body()?.firstOrNull() else null
    }

    /**
     * Updates a user partially.
     *
     * @param userId User ID.
     * @param userUpdate Partial fields to update.
     * @param token JWT token.
     * @return True if successful, false if not.
     */
    suspend fun updateUser(userId: String, userUpdate: UserUpdate, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.updateUser("eq.$userId", userUpdate)
        return response.isSuccessful
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId User ID.
     * @param token JWT token.
     * @return True if successful, false i not.
     */
    suspend fun deleteUser(userId: String, token: String): Boolean {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.deleteUser("eq.$userId")
        return response.isSuccessful
    }

    /**
     * All users with profile type "GESTOR".
     *
     * @param token JWT token.
     * @return List of managers or null.
     */
    suspend fun getAllManagers(token: String): List<User>? {
        val apiService = RetrofitInstance.getApiService(token)
        val response = apiService.getUsersByProfileType("eq.GESTOR")
        return if (response.isSuccessful) response.body() else null
    }
}
