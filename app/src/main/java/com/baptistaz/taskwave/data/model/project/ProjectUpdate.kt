package com.baptistaz.taskwave.data.model.project

/**
 * Used for updating an existing project.
 *
 * @property id_project ID of the project being updated.
 * @property name Updated name.
 * @property description Updated description.
 * @property status Updated status.
 * @property start_date Updated start date.
 * @property end_date Updated end date.
 * @property id_manager Updated manager responsible for the project.
 */
data class ProjectUpdate(
    val id_project: String,
    val name: String,
    val description: String,
    val status: String,
    val start_date: String,
    val end_date: String?,
    val id_manager: String?
)
