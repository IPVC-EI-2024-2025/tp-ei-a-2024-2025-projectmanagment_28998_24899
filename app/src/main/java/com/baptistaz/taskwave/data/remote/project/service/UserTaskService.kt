package com.baptistaz.taskwave.data.remote.project.service

import com.baptistaz.taskwave.data.model.auth.UserTask
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Manages user-task assignments in the 'usertask' table.
 */
interface UserTaskService {

    /**
     * All user-task associations for a given task.
     *
     * @param idTask ID of the task ("eq.<uuid>").
     * @return List of users assigned to the task.
     */
    @GET("usertask")
    suspend fun getUserTasksByTask(
        @Query("id_task") idTask: String
    ): List<UserTask>

    /**
     * Assigns a user to a task.
     *
     * @param userTask UserTask object representing the assignment.
     * @return List containing the inserted assignment.
     */
    @POST("usertask")
    suspend fun assignUserToTask(
        @Body userTask: UserTask
    ): List<UserTask>

    /**
     * Deletes a user-task assignment by its ID.
     *
     * @param idUserTask ID of the assignment ("eq.<uuid>").
     */
    @DELETE("usertask")
    suspend fun deleteUserTask(
        @Query("id_usertask") idUserTask: String
    ): Unit

    /**
     * Updates an existing user-task assignment.
     *
     * @param idUserTask ID of the assignment ("eq.<uuid>").
     * @param userTask Updated data.
     * @return HTTP response indicating success or failure.
     */
    @PATCH("usertask")
    suspend fun updateUserTask(
        @Query("id_usertask") idUserTask: String,
        @Body userTask: UserTask
    ): retrofit2.Response<Unit>
}
