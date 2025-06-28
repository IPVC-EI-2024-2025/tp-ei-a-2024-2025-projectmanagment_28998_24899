package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Task
import retrofit2.http.Body
import retrofit2.http.GET
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

    // Mais tarde podes adicionar update e delete
}
