package com.baptistaz.taskwave.data.model.auth

/**
 * project evaluation made by a project manager.
 *
 * @property id_project ID of the evaluated project.
 * @property id_user ID of the user (team member) being evaluated.
 * @property score score given by the manager.
 * @property comment Optional comment provided by the manager.
 * @property projectName Optional project name, display only.
 *
 * `id_evaluation`, `created_at`, and `weight` -> Supabase.
 */
data class Evaluation(
    val id_project: String,
    val id_user: String,
    val score: Int,
    val comment: String? = null,
    var projectName: String? = null
)
