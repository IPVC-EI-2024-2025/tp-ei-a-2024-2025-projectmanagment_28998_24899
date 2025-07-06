package com.baptistaz.taskwave.ui.home.manager.project.list

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.ui.home.manager.base.BaseManagerBottomNavActivity
import com.baptistaz.taskwave.ui.home.manager.project.ManagerProjectAdapter
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager screen showing the list of their projects.
 */
class ManagerProjectsAreaActivity : BaseManagerBottomNavActivity() {

    override fun getSelectedMenuId() = R.id.nav_manager_area

    private lateinit var btnAtivos: Button
    private lateinit var btnConcluidos: Button
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ManagerProjectAdapter
    private lateinit var projectRepo: ProjectRepository

    private var allProjects: List<Project> = emptyList()
    private var showingActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_projects)

        // UI references
        btnAtivos = findViewById(R.id.btnAtivos)
        btnConcluidos = findViewById(R.id.btnConcluidos)
        rv = findViewById(R.id.rvProjects)

        // RecyclerView setup
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ManagerProjectAdapter(this)
        rv.adapter = adapter

        // Button click: show active projects
        btnAtivos.setOnClickListener {
            showingActive = true
            refreshFilteredList()
            updateButtonStates()
        }

        // Button click: show completed projects
        btnConcluidos.setOnClickListener {
            showingActive = false
            refreshFilteredList()
            updateButtonStates()
        }

        updateButtonStates()
        loadProjectsApi()
    }

    override fun onResume() {
        super.onResume()
        loadProjectsApi()
    }

    /**
     * Loads the projects from API based on manager ID.
     */
    private fun loadProjectsApi() {
        val token = SessionManager.getAccessToken(this) ?: ""
        val managerId = SessionManager.getUserId(this) ?: ""
        projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))

        CoroutineScope(Dispatchers.Main).launch {
            try {
                allProjects = projectRepo.getProjectsByManager(managerId)
                refreshFilteredList()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManagerProjectsAreaActivity,
                    getString(R.string.error_loading_projects, e.message ?: "unknown"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Filters projects by active/completed and updates the list.
     */
    private fun refreshFilteredList() {
        val filtered = if (showingActive)
            allProjects.filter { it.status.equals("active", true) || it.status.equals("em progresso", true) }
        else
            allProjects.filter { it.status.equals("completed", true) || it.status.equals("concluido", true) }

        adapter.updateData(filtered)
    }

    /**
     * Updates the visual state of the Active/Completed filter buttons.
     */
    private fun updateButtonStates() {
        btnAtivos.setBackgroundColor(if (showingActive) getColor(R.color.button_orange) else getColor(R.color.gray))
        btnAtivos.setTextColor(if (showingActive) getColor(R.color.background_white) else getColor(R.color.black))

        btnConcluidos.setBackgroundColor(if (!showingActive) getColor(R.color.button_orange) else getColor(R.color.gray))
        btnConcluidos.setTextColor(if (!showingActive) getColor(R.color.background_white) else getColor(R.color.black))
    }
}
