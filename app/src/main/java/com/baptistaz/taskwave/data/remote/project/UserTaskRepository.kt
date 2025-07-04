package com.baptistaz.taskwave.data.remote.project

import android.util.Log
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance

class UserTaskRepository(private val service: UserTaskService) {
    suspend fun getUserTasksByTask(taskId: String): List<UserTask> =
        service.getUserTasksByTask("eq.$taskId")

    suspend fun assignUserToTask(userTask: UserTask): List<UserTask> =
        service.assignUserToTask(userTask)

    suspend fun deleteUserTask(idUserTask: String) =
        service.deleteUserTask("eq.$idUserTask")

    suspend fun getTasksOfUser(userId: String, token: String): List<UserTask>? {
        val api = RetrofitInstance.getApiService(token)
        val resp = api.getUserTasksForUser("eq.$userId")
        if (!resp.isSuccessful) {
            Log.e("USER_TASK_REPO", resp.errorBody()?.string() ?: "Erro")
            return null
        }
        return resp.body()
    }

    suspend fun updateUserTask(userTask: UserTask): UserTask =
        service.updateUserTask(userTask)

}
