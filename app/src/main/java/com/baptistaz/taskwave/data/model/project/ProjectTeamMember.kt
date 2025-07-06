package com.baptistaz.taskwave.data.model.project

/**
 * Team member assigned to a project.
 *
 * @property id_user ID of the user assigned to the project.
 * @property name Optional name of the user (used for display purposes).
 */
data class ProjectTeamMember(
    val id_user: String,
    val name: String?
)