package com.baptistaz.taskwave.data.model.project

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Represents a project.
 *
 * @property idProject Unique identifier of the project.
 * @property name Project title.
 * @property description Description of the project.
 * @property status Current status of the project ("active", "completed").
 * @property startDate Start date of the project.
 * @property endDate End date for the project.
 * @property idManager ID of the manager responsible for this project.
 */
data class Project(
    @SerializedName("id_project")  val idProject: String,
    @SerializedName("name")       val name: String,
    @SerializedName("description")val description: String?,
    @SerializedName("status")     val status: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date")   val endDate: String?,
    @SerializedName("id_manager") val idManager: String?
) : Serializable
