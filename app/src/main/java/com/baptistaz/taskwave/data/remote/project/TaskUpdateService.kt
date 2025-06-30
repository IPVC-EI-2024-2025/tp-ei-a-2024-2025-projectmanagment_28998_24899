package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.TaskUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskUpdateService {

    @GET("taskupdate")
    suspend fun getUpdatesByTask(
        @Query("id_task") idFilter: String,
        @Query("select")  sel: String = "*"
    ): List<TaskUpdate>

    @Headers("Prefer: return=representation")
    @POST("taskupdate")
    suspend fun createUpdate(@Body upd: TaskUpdate): List<TaskUpdate>

    @PATCH("taskupdate")
    suspend fun updateUpdate(
        @Query("id_update") updFilter: String,
        @Body upd: TaskUpdate
    ): List<TaskUpdate>

    @DELETE("taskupdate")
    suspend fun deleteUpdate(
        @Query("id_update") updId: String
    ): Response<Unit>
}
