package com.baptistaz.taskwave.data.remote

import com.baptistaz.taskwave.data.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @GET("User")
    suspend fun getAllUsers(
        @Header("apikey") apiKey: String,
        @Header("Authorization") auth: String
    ): Response<List<User>>
}
