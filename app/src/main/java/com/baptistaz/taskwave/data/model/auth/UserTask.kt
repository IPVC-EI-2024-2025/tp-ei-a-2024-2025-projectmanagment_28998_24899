package com.baptistaz.taskwave.data.model.auth

import com.baptistaz.taskwave.data.model.task.Task
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Used to assign a task to a user and track its status.
 *
 * @property idUserTask Unique ID of the user-task.
 * @property idUser ID of the assigned user.
 * @property idTask ID of the task assigned.
 * @property registrationDate Date when the task was assigned to the user.
 * @property status ("in_progress", "completed").
 * @property task Optional full task object (includes project).
 */
data class UserTask(
    @SerializedName("id_usertask") val idUserTask: String,
    @SerializedName("id_user") val idUser: String,
    @SerializedName("id_task") val idTask: String,
    @SerializedName("registration_date") val registrationDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("task") val task: Task? = null
) : Serializable
