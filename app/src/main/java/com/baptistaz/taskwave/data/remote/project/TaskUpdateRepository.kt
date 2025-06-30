package com.baptistaz.taskwave.data.remote.project

import com.baptistaz.taskwave.data.model.TaskUpdate

class TaskUpdateRepository(private val service: TaskUpdateService) {
    suspend fun list(taskId: String) =
        service.getUpdatesByTask("eq.$taskId")

    suspend fun create(update: TaskUpdate) =
        service.createUpdate(update).first()

    suspend fun edit(id: String, upd: TaskUpdate) =
        service.updateUpdate("eq.$id", upd).first()

    suspend fun delete(id: String) =
        service.deleteUpdate("eq.$id")

    suspend fun getById(id: String): TaskUpdate =
        service.getById("eq.$id").first()
}
