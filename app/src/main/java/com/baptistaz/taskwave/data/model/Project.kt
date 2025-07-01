package com.baptistaz.taskwave.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Project(
    @SerializedName("id_project")  val idProject: String,
    @SerializedName("name")       val name: String,
    @SerializedName("description")val description: String?,
    @SerializedName("status")     val status: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date")   val endDate: String?,
    @SerializedName("id_manager") val idManager: String?
) : Serializable
