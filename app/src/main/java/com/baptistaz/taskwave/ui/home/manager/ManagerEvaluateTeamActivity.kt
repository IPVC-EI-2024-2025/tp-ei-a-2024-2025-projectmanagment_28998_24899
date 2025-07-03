package com.baptistaz.taskwave.ui.home.manager

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Evaluation
import com.baptistaz.taskwave.data.model.ProjectUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.manager.EvaluationRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ManagerEvaluateTeamActivity : AppCompatActivity() {

    private lateinit var rvTeam: RecyclerView
    private lateinit var btnSubmit: Button
    private lateinit var teamAdapter: TeamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_evaluate_team)

        // Toolbar com "up"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { finish() }
        }

        rvTeam = findViewById(R.id.rvTeam)
        btnSubmit = findViewById(R.id.btn_submit)

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        // Carrega equipa REAL do projeto diretamente da VIEW project_team
        lifecycleScope.launch {
            try {
                val service = RetrofitInstance.getProjectTeamService(token)
                val teamList = service.getTeamMembersByProject("eq.$projectId")
                if (teamList.isEmpty()) {
                    Toast.makeText(this@ManagerEvaluateTeamActivity, "Não há membros para avaliar.", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
                val members = teamList.map { TeamMember(it.id_user, it.name ?: "(sem nome)") }
                teamAdapter = TeamAdapter(members)
                rvTeam.layoutManager = LinearLayoutManager(this@ManagerEvaluateTeamActivity)
                rvTeam.adapter = teamAdapter
            } catch (e: Exception) {
                Log.e("DEBUG-EVAL", "Erro ao carregar equipa: ${e.message}", e)
                Toast.makeText(this@ManagerEvaluateTeamActivity, "Erro ao carregar equipa: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        btnSubmit.setOnClickListener {
            lifecycleScope.launch {
                val ratings = teamAdapter.getRatings() // Map<String, Int>
                val evaluations = ratings.map { (userId, score) ->
                    Evaluation(id_project = projectId, id_user = userId, score = score)
                }

                Log.d("EVAL-REQ", Gson().toJson(evaluations))

                val repo = EvaluationRepository(token)
                try {
                    val success = repo.submitEvaluations(evaluations)
                    if (success) {
                        // Atualizar projeto para Completed
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
                            }
                        } catch (e: Exception) {
                            Log.e("UPDATE_PROJECT", "Erro a atualizar projeto para Completed: ${e.message}", e)
                        }

                        Toast.makeText(this@ManagerEvaluateTeamActivity, "Avaliações submetidas!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ManagerEvaluateTeamActivity, "Erro ao submeter avaliações!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("EVAL-EXCEPTION", "Falhou submit: ${e.message}", e)
                    Toast.makeText(this@ManagerEvaluateTeamActivity, "ERRO: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

// Modelo simples para usar no Adapter
data class TeamMember(val id: String, val name: String, var rating: Int = 3)
