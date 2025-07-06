package com.baptistaz.taskwave.data.model.task

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * Progress update made on a task by a user.
 *
 * @property idUpdate Optional unique ID of the update.
 * @property idTask ID of the task being updated.
 * @property title Title of the update.
 * @property notes Notes or description of the update.
 * @property date Date the update was made.
 * @property location Location related to the task update.
 * @property timeSpent Time spent working on the task.
 * @property photoUrl URL or path to an image evidencing the update.
 */
data class TaskUpdate(
    @SerializedName("id_update") val idUpdate: String? = null,
    @SerializedName("id_task") val idTask: String,
    val title: String,
    val notes: String? = null,
    val date: String? = null,
    val location: String? = null,
    @SerializedName("time_spent") val timeSpent: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null
) : Serializable
