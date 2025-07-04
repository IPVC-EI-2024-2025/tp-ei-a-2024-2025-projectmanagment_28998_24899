package com.baptistaz.taskwave.ui.home.manager

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Evaluation
import com.baptistaz.taskwave.data.model.ProjectUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.manager.EvaluationRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ManagerEvaluateTeamActivity : BaseLocalizedActivity() {

    private lateinit var rvTeam: RecyclerView
    private lateinit var btnSubmit: Button
    private lateinit var teamAdapter: TeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_evaluate_team)

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }

        rvTeam = findViewById(R.id.rvTeam)
        btnSubmit = findViewById(R.id.btn_submit)

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        lifecycleScope.launch {
            try {
                val service = RetrofitInstance.getProjectTeamService(token)
                val teamList = service.getTeamMembersByProject("eq.$projectId")
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

        btnSubmit.setOnClickListener {
            lifecycleScope.launch {
                val ratings = teamAdapter.getRatings()
                val comments = teamAdapter.getComments()

                val evaluations = ratings.map { (userId, score) ->
                    Evaluation(
                        id_project = projectId,
                        id_user = userId,
                        score = score,
                        comment = comments[userId]
                    )
                }

                Log.d("EVAL-REQ", Gson().toJson(evaluations))

                val repo = EvaluationRepository(token)
                try {
                    val success = repo.submitEvaluations(evaluations)
                    if (success) {
                        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
                        try {
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
                                projectRepo.updateProject(projectId, updatedProject)

                                // Atualizar tarefas e user_tasks
                                val taskService = RetrofitInstance.getTaskService(token)
                                val userTaskService = RetrofitInstance.getUserTaskService(token)
                                val userTaskRepo = UserTaskRepository(userTaskService)

                                val projectTasks = taskService.getTasksByProject("eq.$projectId")
                                projectTasks.filter { it.state == "IN_PROGRESS" }.forEach { task ->
                                    taskService.updateTask(task.copy(state = "COMPLETED"))
                                    val userTasks = userTaskService.getUserTasksByTask("eq.${task.idTask}")
                                    userTasks.forEach { ut ->
                                        userTaskRepo.updateUserTask(ut.copy(status = "COMPLETED"))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("UPDATE_PROJECT", "Erro ao atualizar projeto/tarefas: ${e.message}", e)
                        }

                        Toast.makeText(
                            this@ManagerEvaluateTeamActivity,
                            getString(R.string.evaluation_submitted),
                            Toast.LENGTH_SHORT
                        ).show()
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
    }
}

data class TeamMember(val id: String, val name: String, var rating: Int = 3)
