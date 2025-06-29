package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var textName: TextView
    private lateinit var textManager: TextView
    private lateinit var textDescription: TextView
    private lateinit var textStatus: TextView
    private lateinit var textStartDate: TextView
    private lateinit var textEndDate: TextView
    private lateinit var buttonViewTasks: Button
    private lateinit var buttonManageManager: Button
    private lateinit var buttonMarkComplete: Button

    private var managers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Details"

        textName = findViewById(R.id.text_project_name)
        textManager = findViewById(R.id.text_manager)
        textDescription = findViewById(R.id.text_project_description)
        textStatus = findViewById(R.id.text_project_status)
        textStartDate = findViewById(R.id.text_project_start)
        textEndDate = findViewById(R.id.text_project_end)
        buttonViewTasks = findViewById(R.id.button_view_tasks)
        buttonManageManager = findViewById(R.id.button_manage_manager)
        buttonMarkComplete = findViewById(R.id.button_mark_complete)

        // Recebe o Project pelo intent
        val project = intent.getSerializableExtra("project") as? Project
        project?.let { proj ->
            textName.text = proj.name
            textDescription.text = proj.description
            textStatus.text = proj.status
            textStartDate.text = proj.startDate
            textEndDate.text = proj.endDate

            // Buscar managers e mostrar o nome do manager atual
            val token = SessionManager.getAccessToken(this) ?: return
            lifecycleScope.launch {
                managers = UserRepository().getAllManagers(token) ?: emptyList()
                val managerName = managers.firstOrNull { it.id_user == proj.idManager }?.name ?: "No manager"
                textManager.text = "Manager: $managerName"
            }
        }

        buttonViewTasks.setOnClickListener {
            // Lógica: abrir página de tarefas do projeto
            val intent = Intent(this, ProjectTasksActivity::class.java)
            intent.putExtra("project_id", project?.idProject)
            startActivity(intent)
        }

        buttonManageManager.setOnClickListener {
            project?.let { proj ->
                // Avança para o ecrã de gestão de manager (que vais criar já a seguir)
                val intent = Intent(this, ManageManagerActivity::class.java)
                intent.putExtra("project", proj)
                startActivity(intent)
            } ?: Toast.makeText(this, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
        }

        buttonMarkComplete.setOnClickListener {
            // Em breve!
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        // Busca novamente os dados do projeto do backend
        val project = intent.getSerializableExtra("project") as? Project ?: return
        val projectId = project.idProject
        val token = SessionManager.getAccessToken(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val updatedProject = repo.getProjectById(projectId)
            updatedProject?.let { atualizarUI(it) }
        }
    }

    private fun atualizarUI(proj: Project) {
        textName.text = proj.name
        textDescription.text = proj.description
        textStatus.text = proj.status
        textStartDate.text = proj.startDate
        textEndDate.text = proj.endDate
        // Atualiza manager se já tens managers em memória:
        val managerName = managers.firstOrNull { it.id_user == proj.idManager }?.name ?: "No manager"
        textManager.text = "Manager: $managerName"
    }

}
