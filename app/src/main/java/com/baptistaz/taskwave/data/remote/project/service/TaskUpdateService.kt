package com.baptistaz.taskwave.data.remote.project.service

import com.baptistaz.taskwave.data.model.task.TaskUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Manages updates made to tasks.
 */
interface TaskUpdateService {

    /**
     * All updates associated with a given task.
     *
     * @param idFilter Task ID filter ("eq.<uuid>").
     * @param sel Fields to select.
     * @return List of updates for the specified task.
     */
    @GET("taskupdate")
    suspend fun getUpdatesByTask(
        @Query("id_task") idFilter: String,
        @Query("select") sel: String = "*"
    ): List<TaskUpdate>

    /**
     * Creates a new update for a task.
     *
     * @param upd TaskUpdate object to insert.
     * @return List containing the inserted update(s).
     */
    @Headers("Prefer: return=representation")
    @POST("taskupdate")
    suspend fun createUpdate(@Body upd: TaskUpdate): List<TaskUpdate>

    /**
     * Updates an existing task update by ID.
     *
     * @param updFilter Update ID filter ("eq.<uuid>").
     * @param upd Updated TaskUpdate data.
     * @return List containing the updated update(s).
     */
    @PATCH("taskupdate")
    suspend fun updateUpdate(
        @Query("id_update") updFilter: String,
        @Body upd: TaskUpdate
    ): List<TaskUpdate>

    /**
     * Deletes an update by its ID.
     *
     * @param updId Update ID ("eq.<uuid>").
     * @return Response indicating success/failure.
     */
    @DELETE("taskupdate")
    suspend fun deleteUpdate(
        @Query("id_update") updId: String
    ): Response<Unit>

    /**
     * Specific update by its ID.
     *
     * @param idFilter Update ID filter ("eq.<uuid>").
     * @param sel Fields to select (default: all).
     * @return List containing the matched update.
     */
    @GET("taskupdate")
    suspend fun getById(
        @Query("id_update") idFilter: String,
        @Query("select") sel: String = "*"
    ): List<TaskUpdate>
}
