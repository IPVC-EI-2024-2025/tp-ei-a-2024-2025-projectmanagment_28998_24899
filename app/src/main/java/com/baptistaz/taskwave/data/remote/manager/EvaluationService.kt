package com.baptistaz.taskwave.data.remote.manager

import com.baptistaz.taskwave.data.model.Evaluation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EvaluationService {
    @POST("evaluation")
    suspend fun insertEvaluations(
        @Body evaluations: List<Evaluation>
    ): Response<Unit>
}