package com.baptistaz.taskwave.data.remote.common

import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.auth.UserTask
import com.baptistaz.taskwave.data.model.auth.UserUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Generic API operations.
 */
interface ApiService {

    /**
     * All users from the 'utilizador' table.
     */
    @GET("utilizador")
    suspend fun getAllUsers(): Response<List<User>>

    /**
     * user based on their Supabase auth ID.
     *
     * @param authId Auth identifier from Supabase.
     * @param select Fields to select (default: all).
     */
    @GET("utilizador")
    suspend fun getUserByAuthId(
        @Query("auth_id") authId: String,
        @Query("select") select: String = "*"
    ): Response<List<User>>

    /**
     * Creates a new user in the 'utilizador' table.
     *
     * @param user Complete User object to be inserted.
     */
    @POST("utilizador")
    suspend fun createUser(@Body user: User): Response<Unit>

    /**
     * User by their ID.
     *
     * @param id Internal ID of the user.
     * @param select Fields to select (default: all).
     */
    @GET("utilizador")
    suspend fun getUserById(
        @Query("id_user") id: String,
        @Query("select") select: String = "*"
    ): Response<List<User>>

    /**
     * Updates a user partially using a UserUpdate model.
     *
     * @param id User's ID.
     * @param user Partial user data to update.
     */
    @PATCH("utilizador")
    suspend fun updateUser(
        @Query("id_user") id: String,
        @Body user: UserUpdate
    ): Response<Unit>

    /**
     * Deletes a user by their ID.
     *
     * @param id User's ID.
     */
    @DELETE("utilizador")
    suspend fun deleteUser(
        @Query("id_user") id: String
    ): Response<Unit>

    /**
     * All tasks assigned to a specific user, including full task and project info.
     *
     * @param idFilter ID of the user.
     * @param select Join query to retrieve the task and project data.
     */
    @GET("usertask")
    suspend fun getUserTasksForUser(
        @Query("id_user") idFilter: String,
        @Query("select") select: String = "*,task(*,project(*))"
    ): Response<List<UserTask>>

    /**
     * Users filtered by profile type.
     *
     * @param profileType Profile TYPE.
     */
    @GET("utilizador")
    suspend fun getUsersByProfileType(
        @Query("profiletype") profileType: String
    ): Response<List<User>>
}
