package com.baptistaz.taskwave.data.remote.manager

import com.baptistaz.taskwave.data.model.Evaluation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EvaluationService {
    @POST("evaluation")
    suspend fun insertEvaluations(
        @Body evaluations: List<Evaluation>
    ): Response<Unit>

    @GET("evaluation")
    suspend fun getEvaluationsByProject(
        @Query("id_project") projectId: String
    ): List<Evaluation>

    @GET("evaluation")
    suspend fun getEvaluationsByUser(
        @Query("id_user") userId: String
    ): List<Evaluation>
}
