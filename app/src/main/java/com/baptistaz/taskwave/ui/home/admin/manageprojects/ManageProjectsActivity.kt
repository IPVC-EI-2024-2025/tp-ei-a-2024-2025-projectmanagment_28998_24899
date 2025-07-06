package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
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
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageProjectsActivity : BaseLocalizedActivity() {

    private lateinit var textTotal: TextView
    private lateinit var textActive: TextView
    private lateinit var textCompleted: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectAdapter
    private lateinit var inputSearch: EditText
    private var fullProjectList: List<Project> = emptyList()
    private lateinit var spinnerFilter: Spinner

    private var managers: List<User> = emptyList()

    private val token: String by lazy {
        SessionManager.getAccessToken(this) ?: ""
    }

    private val viewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory(
            ProjectRepository(RetrofitInstance.getProjectService(token))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_projects)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_projects_toolbar_title)

        // Cards estatísticos: Total, Ativos, Concluídos
        textTotal = findViewById(R.id.text_value_total)
        textActive = findViewById(R.id.text_value_active)
        textCompleted = findViewById(R.id.text_value_completed)

        // Outros componentes
        inputSearch = findViewById(R.id.input_search)
        spinnerFilter = findViewById(R.id.spinner_filter)
        recyclerView = findViewById(R.id.recycler_projects)

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

            spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            inputSearch.addTextChangedListener {
                applyFilters()
            }

            // Coletando os projetos e atualizando as informações nos cards
            lifecycleScope.launch {
                viewModel.projects.collectLatest {
                    fullProjectList = it
                    applyFilters()

                    // Atualizando os valores dinamicamente
                    textTotal.text = "${viewModel.getTotalCount()}"
                    textActive.text = "${viewModel.getActiveCount()}"
                    textCompleted.text = "${viewModel.getCompletedCount()}"
                    Log.d("DEBUG_VIEW", "Lista atualizada: $it")
                }
            }

            viewModel.loadProjects()

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

    private fun applyFilters() {
        val query = inputSearch.text.toString()
        val selectedStatus = spinnerFilter.selectedItem.toString()

        val filtered = fullProjectList.filter { project ->
            val matchesSearch = project.name.contains(query, ignoreCase = true)
            val matchesStatus = when (selectedStatus) {
                getString(R.string.manage_projects_filter_active) -> project.status.equals("active", true)
                getString(R.string.manage_projects_filter_completed) -> project.status.equals("completed", true)
                else -> true // Todos
            }
            matchesSearch && matchesStatus
        }

        adapter.updateData(filtered)
    }

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
                            getString(R.string.delete_project_error, e.message ?: "Erro desconhecido"),
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
