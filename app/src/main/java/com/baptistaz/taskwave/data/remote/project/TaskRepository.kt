package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Task

class TaskRepository(private val service: TaskService) {
    suspend fun getTasksByProject(projectId: String) = service.getTasksByProject(projectId)
    suspend fun createTask(task: Task) = service.createTask(task)
    suspend fun updateTask(idTask: String, task: Task) =
        service.updateTask("eq.$idTask", task)
    suspend fun deleteTask(idTask: String) =
        service.deleteTask("eq.$idTask")
}
