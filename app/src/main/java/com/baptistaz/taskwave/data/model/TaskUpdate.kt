package com.baptistaz.taskwave.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TaskUpdate(
    @SerializedName("id_update") val idUpdate: String? = null,
    @SerializedName("id_task")   val idTask:  String,
    val title:       String,
    val notes:       String? = null,
    val date:        String? = null,
    val location:    String? = null,
    @SerializedName("time_spent") val timeSpent: String? = null,
    @SerializedName("photo_url")  val photoUrl:  String? = null
) : Serializable

