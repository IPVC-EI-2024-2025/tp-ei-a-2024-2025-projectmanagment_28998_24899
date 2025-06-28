package com.baptistaz.taskwave.data.model

import java.io.Serializable

data class UserTask(
    val id_usertask: String,
    val id_user: String,
    val id_task: String,
    val registration_date: String?,
    val status: String?
) : Serializable
