package com.baptistaz.taskwave.ui.home.admin.assignmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Admin screen to assign or reassign managers to active projects.
 */
class AssignManagerActivity : BaseLocalizedActivity() {

    private lateinit var listProjects: LinearLayout
    private var projects: List<Project> = emptyList()
    private var managers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_manager)

        // Toolbar setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.assign_manager_title)

        listProjects = findViewById(R.id.list_projects_with_manager)

        loadProjectsAndManagers()
    }

    /**
     * Fetches projects and available managers from backend.
     */
    private fun loadProjectsAndManagers() {
        val token = SessionManager.getAccessToken(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val userRepo = UserRepository()

            // Get only active projects with assigned managers
            projects = projectRepo.getAllProjects()
                .filter { !it.idManager.isNullOrEmpty() && it.status == "Active" }

            // Load all users with profile = "GESTOR"
            managers = userRepo.getAllManagers(token) ?: emptyList()

            showProjects()
        }
    }

    /**
     * Dynamically renders project cards and their manager assignment options.
     */
    private fun showProjects() {
        listProjects.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (project in projects) {
            val view = inflater.inflate(R.layout.item_project_assign_manager, listProjects, false)

            // Set project name
            view.findViewById<TextView>(R.id.text_project_name).text = project.name

            // Find and display current manager
            val currentManager = managers.firstOrNull { it.id_user == project.idManager }
            val currentManagerText = getString(
                R.string.current_manager_label,
                currentManager?.name ?: "N/A"
            )
            view.findViewById<TextView>(R.id.text_current_manager).text = currentManagerText

            // Fill spinner with manager names
            val spinner = view.findViewById<Spinner>(R.id.spinner_managers)
            val managerNames = managers.map { it.name }
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, managerNames)

            // Preselect the current manager
            val currentIndex = managers.indexOfFirst { it.id_user == project.idManager }
            if (currentIndex >= 0) spinner.setSelection(currentIndex)

            // Handle update button click
            view.findViewById<Button>(R.id.button_update).setOnClickListener {
                val selectedIndex = spinner.selectedItemPosition
                val selectedManager = managers.getOrNull(selectedIndex)

                // Only proceed if manager is different
                if (selectedManager != null && selectedManager.id_user != project.idManager) {
                    assignManagerToProject(project, selectedManager)
                } else {
                    Toast.makeText(this, getString(R.string.toast_select_different_manager), Toast.LENGTH_SHORT).show()
                }
            }

            listProjects.addView(view)
        }
    }

    /**
     * Sends the updated manager assignment to backend.
     */
    private fun assignManagerToProject(project: Project, manager: User) {
        val token = SessionManager.getAccessToken(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))

            // Create update payload
            val updateData = ProjectUpdate(
                id_project = project.idProject,
                name = project.name,
                description = project.description ?: "",
                status = project.status ?: "",
                start_date = project.startDate ?: "",
                end_date = project.endDate ?: "",
                id_manager = manager.id_user
            )

            // Send update and refresh view
            repo.updateProject(project.idProject, updateData)
            Toast.makeText(this@AssignManagerActivity, getString(R.string.toast_manager_updated), Toast.LENGTH_SHORT).show()
            loadProjectsAndManagers()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
