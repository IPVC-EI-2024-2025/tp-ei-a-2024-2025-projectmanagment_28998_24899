package com.baptistaz.taskwave.ui.home.manager

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.manager.EvaluationRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerProjectDetailsCompletedActivity : BaseLocalizedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_details_completed)

        // Toolbar como ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // podes definir depois o nome do projeto

        // Obter ID do projeto
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
        val userRepo = UserRepository()
        val evaluationRepo = EvaluationRepository(token)

        CoroutineScope(Dispatchers.Main).launch {
            // Detalhes do projeto
            val project = projectRepo.getProjectById(projectId)
            project?.let {
                findViewById<TextView>(R.id.text_project_name).text = it.name
                findViewById<TextView>(R.id.text_project_description).text = it.description ?: ""
                findViewById<TextView>(R.id.text_project_status).text = it.status ?: ""
                findViewById<TextView>(R.id.text_start_date).text = it.startDate ?: ""
                findViewById<TextView>(R.id.text_end_date).text = it.endDate ?: ""

                val managerName = it.idManager?.let { id ->
                    userRepo.getUserById(id, token)?.name
                } ?: getString(R.string.unknown_user)

                findViewById<TextView>(R.id.text_manager).text =
                    getString(R.string.label_manager_with_value, managerName)
            }

            // Lista de tarefas (read-only)
            val rvTasks = findViewById<RecyclerView>(R.id.rvProjectTasks)
            rvTasks.layoutManager = LinearLayoutManager(this@ManagerProjectDetailsCompletedActivity)
            val tasks = taskRepo.getTasksByProject(projectId)
            rvTasks.adapter = ManagerProjectTasksReadOnlyAdapter(tasks)

            // Lista de avaliações
            val rvEvaluations = findViewById<RecyclerView>(R.id.rvEvaluations)
            rvEvaluations.layoutManager = LinearLayoutManager(this@ManagerProjectDetailsCompletedActivity)

            val evaluations = evaluationRepo.getEvaluationsByProject(projectId)
            val evaluationsWithUserNames = evaluations.map { eval ->
                val userName = userRepo.getUserById(eval.id_user, token)?.name
                    ?: getString(R.string.unknown_user)
                eval.copy(id_user = userName)
            }

            rvEvaluations.adapter = EvaluationAdapter(evaluationsWithUserNames)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
