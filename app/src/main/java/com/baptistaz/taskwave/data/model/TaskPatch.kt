package com.baptistaz.taskwave.data.model

data class TaskPatch(
    val title         : String? = null,
    val description   : String? = null,
    val state         : String? = null,
    val creationDate  : String? = null,
    val conclusionDate: String? = null,
    val priority      : String? = null,
    val idProject     : String? = null
)
