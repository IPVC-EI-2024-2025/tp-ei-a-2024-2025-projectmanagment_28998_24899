package com.baptistaz.taskwave.data.model.task

import com.baptistaz.taskwave.data.model.project.Project
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Task(
    @SerializedName("id_task") val idTask: String,
    @SerializedName("id_project") val idProject: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("state") val state: String,          // PENDING | IN_PROGRESS | COMPLETED
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("conclusion_date") val conclusionDate: String?,
    @SerializedName("priority") val priority: String?,      // LOW | MEDIUM | HIGH | null
    /** ★ Virá preenchido apenas quando fizeres o JOIN no select; caso
     *  contrário fica null e o resto da app continua a funcionar. */
    @SerializedName("project") val project: Project? = null
) : Serializable
