package com.baptistaz.taskwave.data.remote

import User
import com.baptistaz.taskwave.data.model.UserUpdate
import com.baptistaz.taskwave.data.model.UserTask
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("utilizador")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("utilizador")
    suspend fun getUserByAuthId(
        @Query("auth_id") authId: String,
        @Query("select") select: String = "*"
    ): Response<List<User>>

    @POST("utilizador")
    suspend fun createUser(@Body user: User): Response<Unit>

    @GET("utilizador")
    suspend fun getUserById(
        @Query("id_user") id: String,
        @Query("select") select: String = "*"
    ): Response<List<User>>

    @PATCH("utilizador")
    suspend fun updateUser(
        @Query("id_user") id: String,
        @Body user: UserUpdate
    ): Response<Unit>

    @DELETE("utilizador")
    suspend fun deleteUser(
        @Query("id_user") id: String
    ): Response<Unit>

    @GET("usertask")
    suspend fun getUserTasksForUser(
        @Query("id_user") idFilter: String,
        @Query("select")
        select: String = "*,task(*,project(*))"
    ): Response<List<UserTask>>

    @GET("utilizador")
    suspend fun getUsersByProfileType(
        @Query("profiletype") profileType: String // ex: "GESTOR"
    ): Response<List<User>>

}