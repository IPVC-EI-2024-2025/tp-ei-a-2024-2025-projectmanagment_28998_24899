package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Task
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskService {
    @GET("task")
    suspend fun getTasksByProject(
        @Query("id_project") idProjectFilter: String,
        @Query("select") select: String = "*"
    ): List<Task>

    @POST("task")
    suspend fun createTask(@Body task: Task): List<Task>

    @PATCH("task")
    suspend fun updateTask(
        @Query("id_task") idTask: String,
        @Body task: Task
    ): List<Task>

    @DELETE("task")
    suspend fun deleteTask(
        @Query("id_task") idTask: String
    ): retrofit2.Response<Unit>

}
