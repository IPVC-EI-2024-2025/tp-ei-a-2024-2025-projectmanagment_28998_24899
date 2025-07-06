package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ManagerProjectDetailsActivity : BaseLocalizedActivity() {
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
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_project_details)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.project_name)

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
                    Toast.makeText(
                        this@ManagerProjectDetailsActivity,
                        getString(R.string.project_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                // 2) Popula UI
                textName.text   = project.name
                textDesc.text   = project.description
                textStatus.text = getString(R.string.project_status) + ": ${project.status}"
                textStart.text  = getString(R.string.start_date) + ": ${project.startDate}"
                textEnd.text    = getString(R.string.end_date) + ": ${project.endDate}"

                // 3) Carrega nome do gestor
                val managerName = project.idManager
                    ?.let { UserRepository().getUserById(it, jwt)?.name }
                    ?: getString(R.string.no_manager)
                textManager.text = getString(R.string.label_manager) + managerName

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
                        Toast.makeText(
                            this@ManagerProjectDetailsActivity,
                            getString(R.string.only_manager_can_edit),
                            Toast.LENGTH_SHORT
                        ).show()
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

                // 5) Lógica de marcação como concluído
                buttonMarkComplete.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val taskRepo = TaskRepository(RetrofitInstance.getTaskService(jwt))
                            val tasks    = taskRepo.getTasksByProject(projectId)
                            val pending  = tasks.filter { it.state != "COMPLETED" }

                            if (pending.isNotEmpty()) {
                                AlertDialog.Builder(this@ManagerProjectDetailsActivity)
                                    .setTitle(getString(R.string.pending_tasks_title))
                                    .setMessage(getString(R.string.alert_pending_tasks, pending.size))
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        lifecycleScope.launch {
                                            pending.forEach { t ->
                                                try { taskRepo.markCompleted(t.idTask) } catch (_: Exception) {}
                                            }
                                            goToEvaluateTeam()
                                        }
                                    }
                                    .setNegativeButton(getString(R.string.no), null)
                                    .show()
                            } else {
                                goToEvaluateTeam()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ManagerProjectDetailsActivity,
                                getString(R.string.error_checking_tasks, e.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ManagerProjectDetailsActivity,
                    getString(R.string.error_loading_data, e.message),
                    Toast.LENGTH_LONG
                ).show()
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
