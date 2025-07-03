package com.baptistaz.taskwave.data.remote.manager

import android.util.Log
import com.baptistaz.taskwave.data.model.Evaluation
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.google.gson.Gson

class EvaluationRepository(private val token: String) {
    private val service = RetrofitInstance.getEvaluationService(token)

    suspend fun submitEvaluations(evaluations: List<Evaluation>): Boolean {
        Log.d("EVAL-REQ", Gson().toJson(evaluations))
        val resp = service.insertEvaluations(evaluations)
        if (!resp.isSuccessful) {
            Log.e("EVALUATION", "Erro ao inserir: ${resp.code()} - ${resp.errorBody()?.string()} - Data: $evaluations")
        }
        return resp.isSuccessful
    }

}