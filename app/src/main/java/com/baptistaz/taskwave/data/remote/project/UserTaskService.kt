package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.UserTask
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserTaskService {
    @GET("usertask")
    suspend fun getUserTasksByTask(
        @Query("id_task") idTask: String
    ): List<UserTask>

    @POST("usertask")
    suspend fun assignUserToTask(
        @Body userTask: UserTask
    ): List<UserTask>

    @DELETE("usertask")
    suspend fun deleteUserTask(
        @Query("id_usertask") idUserTask: String
    ): Unit
}
