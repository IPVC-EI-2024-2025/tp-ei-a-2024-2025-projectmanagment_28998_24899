package com.baptistaz.taskwave.ui.home.manager

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerProjectDetailsCompletedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_details_completed)

        // Toolbar como ActionBar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // ou mete o nome do projeto se quiseres

        // 1. Buscar o PROJECT_ID recebido no intent
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()

        // 2. Buscar token e instanciar os repositórios
        val token = SessionManager.getAccessToken(this) ?: return finish()
        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val userRepo = UserRepository()

        CoroutineScope(Dispatchers.Main).launch {
            // 3. Buscar projeto e mostrar detalhes
            val project = projectRepo.getProjectById(projectId)
            project?.let {
                findViewById<TextView>(R.id.text_project_name).text = it.name
                findViewById<TextView>(R.id.text_project_description).text = it.description ?: ""
                findViewById<TextView>(R.id.text_project_status).text = it.status ?: ""
                findViewById<TextView>(R.id.text_start_date).text = it.startDate ?: ""
                findViewById<TextView>(R.id.text_end_date).text = it.endDate ?: ""
                // Mostrar nome do gestor
                val manager = it.idManager?.let { managerId -> userRepo.getUserById(managerId, token) }
                findViewById<TextView>(R.id.text_manager).text = "Gestor: ${manager?.name ?: "N/A"}"
            }

            // 4. Carregar tarefas do projeto (modo read-only)
            val rv = findViewById<RecyclerView>(R.id.rvProjectTasks)
            rv.layoutManager = LinearLayoutManager(this@ManagerProjectDetailsCompletedActivity)
            // Supõe que já tens um adapter read-only, ex: ProjectTaskReadOnlyAdapter
            val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
            val tasks = taskRepo.getTasksByProject(projectId)
            rv.adapter = ManagerProjectTasksReadOnlyAdapter(tasks)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
