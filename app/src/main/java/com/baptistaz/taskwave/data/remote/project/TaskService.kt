package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.model.TaskPatch
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskService {

    /* ---------- SELECT ---------- */
    @GET("task")
    suspend fun getTasksByProject(
        @Query("id_project") idProjectFilter: String,
        @Query("select")     select: String = "*"
    ): List<Task>

    @GET("task")
    suspend fun getTaskById(
        @Query("id_task") idTaskFilter: String,
        @Query("select")  select: String = "*"
    ): List<Task>

    /* ---------- INSERT ---------- */
    @POST("task")
    suspend fun createTask(@Body task: Task): List<Task>

    /* ---------- UPDATE COMPLETO ---------- */
    @PATCH("task")
    suspend fun putTask(                               // <-- altera nome
        @Query("id_task") idTaskFilter: String,
        @Body             task: Task
    ): List<Task>

    /* ---------- PATCH PARCIAL ---------- */
    @PATCH("task")
    suspend fun patchTask(                             // <-- só campos soltos
        @Query("id_task") idTaskFilter: String,
        @Body             patch: TaskPatch
    ): List<Task>

    /* ---------- DELETE ---------- */
    @DELETE("task")
    suspend fun deleteTask(
        @Query("id_task") idTaskFilter: String
    ): retrofit2.Response<Unit>
}
