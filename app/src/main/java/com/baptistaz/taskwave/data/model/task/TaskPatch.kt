package com.baptistaz.taskwave.data.model.task

/**
 * Data class used for partial updates to a task.
 * All fields are optional and only the non-null ones will be updated.
 *
 * @property title Updated title of the task.
 * @property description Updated description.
 * @property state Updated state.
 * @property creationDate Updated creation date.
 * @property conclusionDate Updated conclusion date.
 * @property priority Updated priority.
 * @property idProject Updated project ID the task belongs to.
 */
data class TaskPatch(
    val title: String? = null,
    val description: String? = null,
    val state: String? = null,
    val creationDate: String? = null,
    val conclusionDate: String? = null,
    val priority: String? = null,
    val idProject: String? = null
)
