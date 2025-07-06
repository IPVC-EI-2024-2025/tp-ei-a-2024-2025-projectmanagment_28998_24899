package com.baptistaz.taskwave.data.remote.project.service

import com.baptistaz.taskwave.data.model.task.Task
import com.baptistaz.taskwave.data.model.task.TaskPatch
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Manages tasks in the 'task' table.
 */
interface TaskService {

    /**
     * All tasks associated with a specific project.
     *
     * @param idProjectFilter Project ID used as filter ("eq.<uuid>").
     * @param select Fields to retrieve.
     */
    @GET("task")
    suspend fun getTasksByProject(
        @Query("id_project") idProjectFilter: String,
        @Query("select") select: String = "*"
    ): List<Task>

    /**
     * Task by its ID.
     *
     * @param idTaskFilter Task ID filter ("eq.<uuid>").
     * @param select Fields to retrieve.
     */
    @GET("task")
    suspend fun getTaskById(
        @Query("id_task") idTaskFilter: String,
        @Query("select") select: String = "*"
    ): List<Task>

    /**
     * All tasks (no filter).
     *
     * @param select Fields to retrieve.
     */
    @GET("task")
    suspend fun getAllTasks(
        @Query("select") select: String = "*"
    ): List<Task>

    /**
     * Creates a new task.
     *
     * @param task Task object to insert.
     * @return List containing the inserted task(s).
     */
    @POST("task")
    suspend fun createTask(@Body task: Task): List<Task>

    /**
     * Replaces all fields of an existing task.
     *
     * @param idTaskFilter Task ID filter ("eq.<uuid>").
     * @param task Full task object to update.
     */
    @PATCH("task")
    suspend fun putTask(
        @Query("id_task") idTaskFilter: String,
        @Body task: Task
    ): List<Task>

    /**
     * Updates only selected fields of a task.
     *
     * @param idTaskFilter Task ID filter ("eq.<uuid>").
     * @param patch Partial fields to update.
     */
    @PATCH("task")
    suspend fun patchTask(
        @Query("id_task") idTaskFilter: String,
        @Body patch: TaskPatch
    ): List<Task>

    /**
     * Deletes a task by its ID.
     *
     * @param idTaskFilter Task ID filter ("eq.<uuid>").
     */
    @DELETE("task")
    suspend fun deleteTask(
        @Query("id_task") idTaskFilter: String
    ): retrofit2.Response<Unit>
}
