package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.UserTask

class UserTaskRepository(private val service: UserTaskService) {
    suspend fun getUserTasksByTask(taskId: String): List<UserTask> =
        service.getUserTasksByTask("eq.$taskId")

    suspend fun assignUserToTask(userTask: UserTask): List<UserTask> =
        service.assignUserToTask(userTask)

    suspend fun deleteUserTask(idUserTask: String) =
        service.deleteUserTask("eq.$idUserTask")
}
