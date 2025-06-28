package com.baptistaz.taskwave.data.model

import java.io.Serializable

data class Task(
    val id_task: String,
    val id_project: String,
    val title: String,
    val description: String,
    val state: String, // "PENDING", "IN_PROGRESS", "COMPLETED"
    val creation_date: String,
    val conclusion_date: String?,
    val priority: String? // "LOW", "MEDIUM", "HIGH" ou null
) : Serializable
