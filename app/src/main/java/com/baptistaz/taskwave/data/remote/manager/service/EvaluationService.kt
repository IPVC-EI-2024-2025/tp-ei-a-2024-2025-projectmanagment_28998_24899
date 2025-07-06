package com.baptistaz.taskwave.data.remote.manager.service

import com.baptistaz.taskwave.data.model.auth.Evaluation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interactes with the 'evaluation' table.
 */
interface EvaluationService {

    /**
     * Inserts a list of evaluations into the DB.
     *
     * @param evaluations List of evaluation objects to be created.
     * @return HTTP response indicating success or failure.
     */
    @POST("evaluation")
    suspend fun insertEvaluations(
        @Body evaluations: List<Evaluation>
    ): Response<Unit>

    /**
     * All evaluations for a specific project.
     *
     * @param projectId ID of the project to filter by.
     * @return List of evaluations linked to the given project.
     */
    @GET("evaluation")
    suspend fun getEvaluationsByProject(
        @Query("id_project") projectId: String
    ): List<Evaluation>

    /**
     * All evaluations received by a specific user.
     *
     * @param userId ID of the user to filter by.
     * @return List of evaluations received by the user.
     */
    @GET("evaluation")
    suspend fun getEvaluationsByUser(
        @Query("id_user") userId: String
    ): List<Evaluation>
}
