package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ManagerProjectDetailsActivity : AppCompatActivity() {
    private lateinit var textName: TextView
    private lateinit var textDesc: TextView
    private lateinit var textStatus: TextView
    private lateinit var textManager: TextView
    private lateinit var textStart: TextView
    private lateinit var textEnd: TextView
    private lateinit var buttonViewTasks: Button
    private lateinit var buttonEditProject: Button
    private lateinit var buttonMarkComplete: Button

    private lateinit var projectId: String
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_details)

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Liga views
        textName           = findViewById(R.id.text_project_name)
        textDesc           = findViewById(R.id.text_project_description)
        textStatus         = findViewById(R.id.text_project_status)
        textManager        = findViewById(R.id.text_manager)
        textStart          = findViewById(R.id.text_project_start)
        textEnd            = findViewById(R.id.text_project_end)
        buttonViewTasks    = findViewById(R.id.button_view_tasks)
        buttonEditProject  = findViewById(R.id.button_edit_project)
        buttonMarkComplete = findViewById(R.id.button_mark_complete)

        // Carrega argumentos e token
        projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        token     = SessionManager.getAccessToken(this) ?: return finish()

        reloadProject()
    }

    override fun onResume() {
        super.onResume()
        reloadProject()
    }

    private fun reloadProject() {
        val jwt = token ?: return
        lifecycleScope.launch {
            try {
                // 1) Busca o projeto atualizado
                val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(jwt))
                val project     = projectRepo.getProjectById(projectId)
                if (project == null) {
                    Toast.makeText(this@ManagerProjectDetailsActivity, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // 2) Popula UI
                textName.text   = project.name
                textDesc.text   = project.description
                textStatus.text = project.status
                textStart.text  = project.startDate
                textEnd.text    = project.endDate

                // 3) Carrega nome do gestor
                val managerName = project.idManager
                    ?.let { UserRepository().getUserById(it, jwt)?.name }
                    ?: "No manager"
                textManager.text = getString(R.string.manager_label, managerName)

                // 4) Verifica se o utilizador é de facto o gestor
                val myId      = SessionManager.getUserId(this@ManagerProjectDetailsActivity)
                val isManager = project.idManager == myId

                // Editar projeto
                buttonEditProject.isEnabled = isManager
                buttonEditProject.alpha     = if (isManager) 1f else 0.5f
                buttonEditProject.setOnClickListener {
                    if (isManager) {
                        Intent(this@ManagerProjectDetailsActivity, ManagerEditProjectActivity::class.java).also {
                            it.putExtra("project", project)
                            startActivity(it)
                        }
                    } else {
                        Toast.makeText(this@ManagerProjectDetailsActivity,
                            "Só o gestor deste projeto pode editar.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                // Ver tarefas do projeto
                buttonViewTasks.setOnClickListener {
                    Intent(this@ManagerProjectDetailsActivity, ManagerProjectTasksActivity::class.java).also {
                        it.putExtra("PROJECT_ID", projectId)
                        it.putExtra("READ_ONLY", !isManager)
                        startActivity(it)
                    }
                }

                // Mostrar ou ocultar botão Concluir
                buttonMarkComplete.visibility = if (project.status.equals("Active", true) && isManager) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // 5) Lógica de confirmação e marcação de tarefas pendentes
                buttonMarkComplete.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            // Instancia o repositório de tasks
                            val taskRepo = TaskRepository(RetrofitInstance.getTaskService(jwt))
                            val tasks    = taskRepo.getTasksByProject(projectId)
                            val pending  = tasks.filter { it.state != "COMPLETED" }

                            if (pending.isNotEmpty()) {
                                // Há tarefas pendentes: alerta
                                AlertDialog.Builder(this@ManagerProjectDetailsActivity)
                                    .setTitle("Tarefas pendentes")
                                    .setMessage("Existem ${pending.size} tarefas pendentes neste projeto.\n\nDeseja marcá-las todas como concluídas e avançar para avaliação?")
                                    .setPositiveButton("Sim") { _, _ ->
                                        lifecycleScope.launch {
                                            // Marca cada pending como completed
                                            pending.forEach { t ->
                                                try { taskRepo.markCompleted(t.idTask) } catch (_: Exception) {}
                                            }
                                            goToEvaluateTeam()
                                        }
                                    }
                                    .setNegativeButton("Não", null)
                                    .show()
                            } else {
                                // Sem pendentes: avança
                                goToEvaluateTeam()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@ManagerProjectDetailsActivity,
                                "Erro ao verificar tarefas: ${e.message}",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@ManagerProjectDetailsActivity,
                    "Erro ao carregar dados: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToEvaluateTeam() {
        Intent(this, ManagerEvaluateTeamActivity::class.java).also {
            it.putExtra("PROJECT_ID", projectId)
            startActivity(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
