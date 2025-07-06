package com.baptistaz.taskwave.ui.home.manager.evaluations

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.Evaluation
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.manager.repository.EvaluationRepository
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.ui.home.manager.project.list.ManagerProjectsAreaActivity
import com.baptistaz.taskwave.ui.home.manager.team.TeamAdapter
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * Activity used by project managers to evaluate team members at the end of a project.
 */
class ManagerEvaluateTeamActivity : BaseLocalizedActivity() {

    private lateinit var rvTeam: RecyclerView
    private lateinit var btnSubmit: Button
    private lateinit var teamAdapter: TeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_evaluate_team)

        // Set back navigation on toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }

        rvTeam = findViewById(R.id.rvTeam)
        btnSubmit = findViewById(R.id.btn_submit)

        // Retrieve required project ID and auth token
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        loadTeamMembers(projectId, token)

        // Handle evaluation submission
        btnSubmit.setOnClickListener {
            submitEvaluationsAndFinalizeProject(projectId, token)
        }
    }

    /**
     * Loads team members associated with the project.
     */
    private fun loadTeamMembers(projectId: String, token: String) {
        lifecycleScope.launch {
            try {
                val teamService = RetrofitInstance.getProjectTeamService(token)
                val teamList = teamService.getTeamMembersByProject("eq.$projectId")

                if (teamList.isEmpty()) {
                    Toast.makeText(
                        this@ManagerEvaluateTeamActivity,
                        getString(R.string.no_members_to_evaluate),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return@launch
                }

                val members = teamList.map { TeamMember(it.id_user, it.name ?: "(sem nome)") }
                teamAdapter = TeamAdapter(members)
                rvTeam.layoutManager = LinearLayoutManager(this@ManagerEvaluateTeamActivity)
                rvTeam.adapter = teamAdapter

            } catch (e: Exception) {
                Log.e("DEBUG-EVAL", "Erro ao carregar equipa: ${e.message}", e)
                Toast.makeText(
                    this@ManagerEvaluateTeamActivity,
                    getString(R.string.error_loading_team, e.message),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    /**
     * Submits the evaluation and updates the project and its tasks to completed.
     */
    private fun submitEvaluationsAndFinalizeProject(projectId: String, token: String) {
        lifecycleScope.launch {
            val ratings = teamAdapter.getRatings()
            val comments = teamAdapter.getComments()

            // Build evaluation list from ratings and comments
            val evaluations = ratings.map { (userId, score) ->
                Evaluation(
                    id_project = projectId,
                    id_user = userId,
                    score = score,
                    comment = comments[userId]
                )
            }

            Log.d("EVAL-REQ", Gson().toJson(evaluations))

            val evalRepo = EvaluationRepository(token)
            try {
                val success = evalRepo.submitEvaluations(evaluations)
                if (success) {
                    updateProjectAndTasksToCompleted(projectId, token)
                    Toast.makeText(
                        this@ManagerEvaluateTeamActivity,
                        getString(R.string.evaluation_submitted),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate back to project list
                    startActivity(Intent(this@ManagerEvaluateTeamActivity, ManagerProjectsAreaActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(
                        this@ManagerEvaluateTeamActivity,
                        getString(R.string.error_submitting_evaluation),
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("EVAL-EXCEPTION", "Falhou submit: ${e.message}", e)
                Toast.makeText(
                    this@ManagerEvaluateTeamActivity,
                    getString(R.string.error_generic_with_message, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Updates the project status to "Completed" and marks all tasks and user-task relations as completed.
     */
    private suspend fun updateProjectAndTasksToCompleted(projectId: String, token: String) {
        try {
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
            val userTaskService = RetrofitInstance.getUserTaskService(token)
            val userTaskRepo = UserTaskRepository(userTaskService)

            val currentProject = projectRepo.getProjectById(projectId)
            if (currentProject != null) {
                val updatedProject = ProjectUpdate(
                    id_project = currentProject.idProject,
                    name = currentProject.name ?: "",
                    description = currentProject.description ?: "",
                    status = "Completed",
                    start_date = currentProject.startDate ?: "",
                    end_date = currentProject.endDate ?: "",
                    id_manager = currentProject.idManager
                )

                // Update project status
                projectRepo.updateProject(projectId, updatedProject)

                // Mark all tasks as completed
                val projectTasks = taskRepo.getTasksByProject(projectId)
                projectTasks.filter { it.state == "IN_PROGRESS" }.forEach { task ->
                    taskRepo.markCompleted(task.idTask)

                    // Mark user-task relations as completed
                    val userTasks = userTaskService.getUserTasksByTask("eq.${task.idTask}")
                    userTasks.forEach { ut ->
                        userTaskRepo.updateUserTask(ut.copy(status = "COMPLETED"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("UPDATE_PROJECT", "Erro ao atualizar projeto/tarefas: ${e.message}", e)
        }
    }
}

/**
 * Simple data class to represent team members being evaluated.
 */
data class TeamMember(
    val id: String,
    val name: String,
    var rating: Int = 3 // default
)
