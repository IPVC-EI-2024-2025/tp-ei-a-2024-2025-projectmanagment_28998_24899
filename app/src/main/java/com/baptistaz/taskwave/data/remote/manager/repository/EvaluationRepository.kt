package com.baptistaz.taskwave.data.remote.manager.repository

import android.util.Log
import com.baptistaz.taskwave.data.model.auth.Evaluation
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.google.gson.Gson

/**
 * Repository for performing operations related to project evaluations.
 *
 * @property token JWT token for authenticated access.
 */
class EvaluationRepository(private val token: String) {

    private val service = RetrofitInstance.getEvaluationService(token)

    /**
     * Submits a list of evaluations to the backend.
     *
     * @param evaluations List of evaluations to be inserted.
     * @return True if the request was successful, false if not.
     */
    suspend fun submitEvaluations(evaluations: List<Evaluation>): Boolean {
        Log.d("EVAL-REQ", Gson().toJson(evaluations))

        val resp = service.insertEvaluations(evaluations)

        if (!resp.isSuccessful) {
            Log.e(
                "EVALUATION",
                "Failed to insert evaluations: ${resp.code()} - ${resp.errorBody()?.string()} - Data: $evaluations"
            )
        }

        return resp.isSuccessful
    }

    /**
     * All evaluations related to a given project.
     *
     * @param projectId Project ID.
     * @return List of evaluations.
     */
    suspend fun getEvaluationsByProject(projectId: String): List<Evaluation> {
        return try {
            service.getEvaluationsByProject("eq.$projectId")
        } catch (e: Exception) {
            Log.e("EVALUATION", "Error fetching project evaluations: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * All evaluations received by a given user.
     *
     * @param userId User ID.
     * @return List of evaluations by user.
     */
    suspend fun getEvaluationsByUser(userId: String): List<Evaluation> {
        return try {
            service.getEvaluationsByUser("eq.$userId")
        } catch (e: Exception) {
            Log.e("EVALUATION", "Error fetching user evaluations: ${e.message}", e)
            emptyList()
        }
    }
}
