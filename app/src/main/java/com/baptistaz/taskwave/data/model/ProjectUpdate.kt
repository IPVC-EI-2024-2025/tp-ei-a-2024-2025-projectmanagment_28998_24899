package com.baptistaz.taskwave.data.model

data class ProjectUpdate(
    val id_project: String,
    val name: String,
    val description: String,
    val status: String,
    val start_date: String,
    val end_date: String?,
    val id_manager: String?
)
