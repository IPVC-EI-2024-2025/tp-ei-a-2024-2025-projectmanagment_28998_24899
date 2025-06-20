package com.baptistaz.taskwave.data.model

import java.io.Serializable

data class Project(
    val id_project: String,
    val name: String,
    val description: String,
    val status: String,
    val start_date: String,
    val end_date: String
) : Serializable

