package com.baptistaz.taskwave.data.remote.project.repository

import com.baptistaz.taskwave.data.model.auth.UserTask
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.service.UserTaskService

/**
 * Manages user-task assignments.
 * API operations to UserTaskService and user-specific queries via ApiService.
 */
class UserTaskRepository(private val service: UserTaskService) {

    /**
     * All user-task assignments for a given task.
     *
     * @param taskId ID of the task.
     * @return List of UserTask objects.
     */
    suspend fun getUserTasksByTask(taskId: String): List<UserTask> =
        service.getUserTasksByTask("eq.$taskId")

    /**
     * Assigns a user to a task.
     *
     * @param userTask UserTask object representing the assignment.
     * @return List containing the created assignment.
     */
    suspend fun assignUserToTask(userTask: UserTask): List<UserTask> =
        service.assignUserToTask(userTask)

    /**
     * Deletes a user-task assignment by its ID.
     *
     * @param idUserTask ID of the user-task assignment.
     */
    suspend fun deleteUserTask(idUserTask: String) =
        service.deleteUserTask("eq.$idUserTask")

    /**
     * All tasks assigned to a specific user (via ApiService).
     *
     * @param userId ID of the user.
     * @param token JWT token for authentication.
     * @return List of UserTask objects or null on failure.
     */
    suspend fun getTasksOfUser(userId: String, token: String): List<UserTask>? {
        val api = RetrofitInstance.getApiService(token)
        val resp = api.getUserTasksForUser("eq.$userId")
        return if (resp.isSuccessful) resp.body() else null
    }

    /**
     * Updates an existing user-task assignment.
     *
     * @param userTask Updated UserTask object.
     * @return True if the update was successful, false if not.
     */
    suspend fun updateUserTask(userTask: UserTask): Boolean {
        val response = service.updateUserTask(userTask.idUserTask, userTask)
        return response.isSuccessful
    }
}
