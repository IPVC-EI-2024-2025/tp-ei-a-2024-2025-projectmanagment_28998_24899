package com.baptistaz.taskwave.data.remote.project.repository

import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.project.service.TaskUpdateService

/**
 * Repository for handling updates to tasks.
 * API operations to TaskUpdateService.
 */
class TaskUpdateRepository(private val service: TaskUpdateService) {

    /**
     * Lists all updates associated with a specific task.
     *
     * @param taskId ID of the task.
     * @return List of updates.
     */
    suspend fun list(taskId: String): List<TaskUpdate> =
        service.getUpdatesByTask("eq.$taskId")

    /**
     * Creates a new task update.
     *
     * @param update TaskUpdate object to insert.
     * @return The inserted update.
     */
    suspend fun create(update: TaskUpdate): TaskUpdate =
        service.createUpdate(update).first()

    /**
     * Updates an existing task update by ID.
     *
     * @param id ID of the update.
     * @param upd Updated TaskUpdate object.
     * @return The updated update.
     */
    suspend fun edit(id: String, upd: TaskUpdate): TaskUpdate =
        service.updateUpdate("eq.$id", upd).first()

    /**
     * Deletes a task update by ID.
     *
     * @param id ID of the update to delete.
     */
    suspend fun delete(id: String) =
        service.deleteUpdate("eq.$id")

    /**
     * Task update by its ID.
     *
     * @param id ID of the update.
     * @return The corresponding TaskUpdate.
     */
    suspend fun getById(id: String): TaskUpdate =
        service.getById("eq.$id").first()
}
