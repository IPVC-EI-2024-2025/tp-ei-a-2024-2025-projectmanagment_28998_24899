package com.baptistaz.taskwave.data.model

import java.io.Serializable

data class TaskWithUser(
    val task: Task,
    val responsavel: String
) : Serializable
