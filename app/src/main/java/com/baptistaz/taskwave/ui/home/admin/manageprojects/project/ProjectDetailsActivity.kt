package com.baptistaz.taskwave.ui.home.admin.manageprojects.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageprojects.overview.ManageManagerActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Admin screen for viewing and managing project details.
 */
class ProjectDetailsActivity : BaseLocalizedActivity() {

    private lateinit var textName: TextView
    private lateinit var textManager: TextView
    private lateinit var textDesc: TextView
    private lateinit var textStatus: TextView
    private lateinit var textStartDate: TextView
    private lateinit var textEndDate: TextView
    private lateinit var buttonTasks: Button
    private lateinit var buttonMgr: Button
    private lateinit var buttonDone: Button

    private var managers: List<User> = emptyList()
    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar_project_details))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Bind views
        textName = findViewById(R.id.text_project_name)
        textManager = findViewById(R.id.text_manager)
        textDesc = findViewById(R.id.text_project_description)
        textStatus = findViewById(R.id.text_project_status)
        textStartDate = findViewById(R.id.text_project_start)
        textEndDate = findViewById(R.id.text_project_end)
        buttonTasks = findViewById(R.id.button_view_tasks)
        buttonMgr = findViewById(R.id.button_manage_manager)
        buttonDone = findViewById(R.id.button_mark_complete)

        // Get project from intent
        project = intent.getSerializableExtra("project") as? Project
            ?: return finish()

        val token = SessionManager.getAccessToken(this) ?: return

        // Load managers and update UI
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            atualizarUI(project)
        }

        // View project tasks
        buttonTasks.setOnClickListener {
            val intent = Intent(this, ProjectTasksActivity::class.java).apply {
                putExtra("project_id", project.idProject)
                putExtra("project_status", project.status)
            }
            startActivity(intent)
        }

        // Manage assigned manager
        buttonMgr.setOnClickListener {
            val intent = Intent(this, ManageManagerActivity::class.java)
                .putExtra("project", project)
            startActivity(intent)
        }

        // Mark project and its tasks as completed
        buttonDone.setOnClickListener {
            val token = SessionManager.getAccessToken(this) ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
                    val userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
                    val tasks = taskRepo.getTasksByProject(project.idProject)
                    val pending = tasks.filter { it.state != "COMPLETED" }

                    if (pending.isNotEmpty()) {
                        AlertDialog.Builder(this@ProjectDetailsActivity)
                            .setTitle(getString(R.string.project_details_button_complete))
                            .setMessage(getString(R.string.project_details_pending_tasks, pending.size))
                            .setPositiveButton(getString(R.string.delete_project_confirm_yes)) { _, _ ->
                                lifecycleScope.launch {
                                    // Mark all pending tasks and their assignments as completed
                                    pending.forEach { t ->
                                        try {
                                            taskRepo.markCompleted(t.idTask)
                                            userTaskRepo.getUserTasksByTask(t.idTask).forEach { ut ->
                                                userTaskRepo.updateUserTask(ut.copy(status = "COMPLETED"))
                                            }
                                        } catch (_: Exception) {}
                                    }
                                    concluirProjeto(token)
                                }
                            }
                            .setNegativeButton(getString(R.string.delete_project_confirm_no), null)
                            .show()
                    } else {
                        concluirProjeto(token)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ProjectDetailsActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
            repo.getProjectById(project.idProject)?.let {
                project = it
                atualizarUI(it)
            }
        }
    }

    /**
     * Updates UI with project details.
     */
    private fun atualizarUI(p: Project) {
        textName.text = p.name
        textDesc.text = p.description
        textStatus.text = p.status
        textStartDate.text = p.startDate
        textEndDate.text = p.endDate

        val mgrName = managers.firstOrNull { it.id_user == p.idManager }?.name
            ?: getString(R.string.project_details_no_manager)
        textManager.text = getString(R.string.project_details_manager) + mgrName

        // Hide buttons if project is already completed
        buttonDone.visibility = if (p.status.equals("Completed", ignoreCase = true)) View.GONE else View.VISIBLE
        buttonMgr.visibility = if (p.status.equals("Completed", ignoreCase = true)) View.GONE else View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }

    /**
     * Marks the project as completed by updating its status.
     */
    private suspend fun concluirProjeto(token: String) {
        try {
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val updated = ProjectUpdate(
                id_project = project.idProject,
                name = project.name ?: "",
                description = project.description ?: "",
                status = "Completed",
                start_date = project.startDate ?: "",
                end_date = project.endDate ?: "",
                id_manager = project.idManager
            )
            projectRepo.updateProject(project.idProject, updated)
            Toast.makeText(this, getString(R.string.project_details_completed_success), Toast.LENGTH_SHORT).show()
            project = project.copy(status = "Completed")
            atualizarUI(project)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.project_details_completed_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }
}
