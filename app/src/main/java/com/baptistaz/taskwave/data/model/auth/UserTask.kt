package com.baptistaz.taskwave.data.model.auth

import com.baptistaz.taskwave.data.model.task.Task
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserTask(
    @SerializedName("id_usertask")     val idUserTask: String,
    @SerializedName("id_user")         val idUser: String,
    @SerializedName("id_task")         val idTask: String,
    @SerializedName("registration_date") val registrationDate: String?,
    @SerializedName("status")          val status: String?,
    /** ★ Task completa (já com Project) quando o JOIN é pedido; senão null. */
    @SerializedName("task")            val task: Task? = null
) : Serializable
