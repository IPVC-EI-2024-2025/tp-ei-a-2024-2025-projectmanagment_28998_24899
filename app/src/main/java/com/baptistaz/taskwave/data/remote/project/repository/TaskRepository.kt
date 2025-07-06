package com.baptistaz.taskwave.data.remote.project.repository

import com.baptistaz.taskwave.data.model.task.Task
import com.baptistaz.taskwave.data.model.task.TaskPatch
import com.baptistaz.taskwave.data.remote.project.service.TaskService

/**
 * Handles task-related operations.
 * API operations to TaskService.
 */
class TaskRepository(private val service: TaskService) {

    /**
     * All tasks for a specific project.
     *
     * @param projectId ID of the project.
     * @return List of tasks assigned to the project.
     */
    suspend fun getTasksByProject(projectId: String): List<Task> {
        return service.getTasksByProject("eq.$projectId") ?: emptyList()
    }

    /**
     * Single task by its ID.
     *
     * @param id ID of the task.
     * @return Task if found, null if not.
     */
    suspend fun getTaskById(id: String): Task? =
        service.getTaskById("eq.$id").firstOrNull()

    /**
     * Creates a new task.
     *
     * @param task Task object to be inserted.
     * @return List containing the inserted task(s).
     */
    suspend fun createTask(task: Task): List<Task> =
        service.createTask(task)

    /**
     * Fully updates a task.
     *
     * @param id ID of the task to update.
     * @param task Updated task data.
     * @return List containing the updated task.
     */
    suspend fun updateTask(id: String, task: Task): List<Task> =
        service.putTask("eq.$id", task)

    /**
     * Deletes a task by ID.
     *
     * @param id ID of the task to delete.
     */
    suspend fun deleteTask(id: String) =
        service.deleteTask("eq.$id")

    /**
     * Marks a task as COMPLETED.
     *
     * @param id ID of the task to update.
     * @return List containing the updated task.
     */
    suspend fun markCompleted(id: String): List<Task> =
        service.patchTask("eq.$id", TaskPatch(state = "COMPLETED"))

    /**
     * All tasks in the system.
     *
     * @return List of all tasks.
     */
    suspend fun getAllTasks(): List<Task> {
        return service.getAllTasks()
    }
}
