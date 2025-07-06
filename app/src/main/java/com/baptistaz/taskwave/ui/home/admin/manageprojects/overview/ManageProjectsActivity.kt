package com.baptistaz.taskwave.ui.home.admin.manageprojects.overview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageprojects.project.CreateProjectActivity
import com.baptistaz.taskwave.ui.home.admin.manageprojects.project.ProjectAdapter
import com.baptistaz.taskwave.ui.home.admin.manageprojects.viewmodel.ProjectViewModel
import com.baptistaz.taskwave.ui.home.admin.manageprojects.viewmodel.ProjectViewModelFactory
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Admin screen for managing all projects.
 * Allows filtering, searching, deleting, and creating new projects.
 * Displays total, active, and completed project counts.
 */
class ManageProjectsActivity : BaseLocalizedActivity() {

    private lateinit var textTotal: TextView
    private lateinit var textActive: TextView
    private lateinit var textCompleted: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectAdapter
    private lateinit var inputSearch: EditText
    private lateinit var spinnerFilter: Spinner

    private var fullProjectList: List<Project> = emptyList()
    private var managers: List<User> = emptyList()

    private val token: String by lazy {
        SessionManager.getAccessToken(this) ?: ""
    }

    private val viewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory(ProjectRepository(RetrofitInstance.getProjectService(token)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)

        // Set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_projects)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_projects_toolbar_title)

        // Initialize views
        textTotal = findViewById(R.id.text_value_total)
        textActive = findViewById(R.id.text_value_active)
        textCompleted = findViewById(R.id.text_value_completed)
        inputSearch = findViewById(R.id.input_search)
        spinnerFilter = findViewById(R.id.spinner_filter)
        recyclerView = findViewById(R.id.recycler_projects)

        // Load managers and initialize adapter
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            adapter = ProjectAdapter(
                emptyList(),
                managers,
                context = this@ManageProjectsActivity,
                onDelete = { project -> eliminarProjeto(project) }
            )
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@ManageProjectsActivity)

            // Set up spinner filter
            val options = listOf(
                getString(R.string.manage_projects_filter_all),
                getString(R.string.manage_projects_filter_active),
                getString(R.string.manage_projects_filter_completed)
            )
            spinnerFilter.adapter = ArrayAdapter(
                this@ManageProjectsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                options
            )

            // Filter on selection change
            spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Filter on search input
            inputSearch.addTextChangedListener { applyFilters() }

            // Observe project list and update UI
            lifecycleScope.launch {
                viewModel.projects.collectLatest {
                    fullProjectList = it
                    applyFilters()
                    textTotal.text = "${viewModel.getTotalCount()}"
                    textActive.text = "${viewModel.getActiveCount()}"
                    textCompleted.text = "${viewModel.getCompletedCount()}"
                    Log.d("DEBUG_VIEW", "Project list updated: $it")
                }
            }

            viewModel.loadProjects()

            // Button to create new project
            findViewById<Button>(R.id.button_new_project).setOnClickListener {
                val intent = Intent(this@ManageProjectsActivity, CreateProjectActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProjects()
    }

    /**
     * Filters the project list based on status and search input.
     */
    private fun applyFilters() {
        val query = inputSearch.text.toString()
        val selectedStatus = spinnerFilter.selectedItem.toString()

        val filtered = fullProjectList.filter { project ->
            val matchesSearch = project.name.contains(query, ignoreCase = true)
            val matchesStatus = when (selectedStatus) {
                getString(R.string.manage_projects_filter_active) -> project.status.equals("active", true)
                getString(R.string.manage_projects_filter_completed) -> project.status.equals("completed", true)
                else -> true
            }
            matchesSearch && matchesStatus
        }

        adapter.updateData(filtered)
    }

    /**
     * Deletes the selected project after confirmation.
     */
    private fun eliminarProjeto(project: Project) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_project_title))
            .setMessage(getString(R.string.project_tasks_delete_confirm, project.name))
            .setPositiveButton(getString(R.string.delete_project_confirm_yes)) { _, _ ->
                lifecycleScope.launch {
                    try {
                        viewModel.deleteProject(project.idProject)
                        viewModel.loadProjects()
                        Toast.makeText(
                            this@ManageProjectsActivity,
                            getString(R.string.delete_project_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ManageProjectsActivity,
                            getString(R.string.delete_project_error, e.message ?: "Unknown error"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.delete_project_confirm_no), null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
