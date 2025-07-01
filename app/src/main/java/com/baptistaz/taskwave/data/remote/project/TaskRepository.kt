package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.model.TaskPatch

class TaskRepository(private val service: TaskService) {

    /* ---------- CRUD ---------- */
    suspend fun getTasksByProject(projectId: String): List<Task> {
        return service.getTasksByProject("eq.$projectId") ?: emptyList()
    }

    suspend fun getTaskById(id: String): Task? =
        service.getTaskById("eq.$id").firstOrNull()

    suspend fun createTask(task: Task): List<Task> =
        service.createTask(task)

    /** Atualização total (todos os campos) */
    suspend fun updateTask(id: String, task: Task): List<Task> =
        service.putTask("eq.$id", task)

    suspend fun deleteTask(id: String) =
        service.deleteTask("eq.$id")

    /* ---------- Helpers ---------- */
    /** Altera apenas o estado para COMPLETED */
    suspend fun markCompleted(id: String): List<Task> =
        service.patchTask("eq.$id", TaskPatch(state = "COMPLETED"))
}
