package com.baptistaz.taskwave.data.model.auth

data class Evaluation(
    val id_project: String,
    val id_user: String,
    val score: Int,
    val comment: String? = null,
    var projectName: String? = null
    // Os campos id_evaluation, created_at, weight são preenchidos pelo Supabase/BD
)
