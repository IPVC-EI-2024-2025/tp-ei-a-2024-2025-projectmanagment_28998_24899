package com.baptistaz.taskwave.data.model.task

import java.io.Serializable

/**
 * Combines a task with its responsible user (by name).
 * Used for display purposes.
 *
 * @property task The task details.
 * @property responsavel Name of the user responsible for the task.
 */
data class TaskWithUser(
    val task: Task,
    val responsavel: String
) : Serializable
