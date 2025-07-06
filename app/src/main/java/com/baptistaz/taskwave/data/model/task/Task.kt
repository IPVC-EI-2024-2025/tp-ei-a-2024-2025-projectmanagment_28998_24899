package com.baptistaz.taskwave.data.model.task

import com.baptistaz.taskwave.data.model.project.Project
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents a task within a project.
 *
 * @property idTask Unique identifier of the task.
 * @property idProject ID of the project this task belongs to.
 * @property title Title of the task.
 * @property description task description.
 * @property state Current state of the task: IN_PROGRESS, or COMPLETED.
 * @property creationDate Date when the task was created.
 * @property conclusionDate Date when the task was concluded.
 * @property priority Priority level: LOW, MEDIUM, or HIGH.
 * @property project Full project object.
 */
data class Task(
    @SerializedName("id_task") val idTask: String,
    @SerializedName("id_project") val idProject: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("state") val state: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("conclusion_date") val conclusionDate: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("project") val project: Project? = null
) : Serializable
