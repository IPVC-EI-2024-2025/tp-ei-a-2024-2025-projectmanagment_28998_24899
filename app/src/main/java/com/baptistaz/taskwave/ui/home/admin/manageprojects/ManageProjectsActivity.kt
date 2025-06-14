package com.baptistaz.taskwave.ui.home.admin.manageprojects

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageProjectsActivity : AppCompatActivity() {

    private lateinit var textTotal: TextView
    private lateinit var textActive: TextView
    private lateinit var textCompleted: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProjectAdapter
    private lateinit var inputSearch: EditText
    private var fullProjectList: List<Project> = emptyList()
    private lateinit var spinnerFilter: Spinner

    private val token: String by lazy {
        SessionManager.getAccessToken(this) ?: ""
    }

    private val viewModel: ProjectViewModel by viewModels {
        Log.d("TOKEN_DEBUG", "Token atual: $token")
        ProjectViewModelFactory(
            ProjectRepository(RetrofitInstance.getProjectService(token))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Projects"

        val cardTotal = findViewById<androidx.cardview.widget.CardView>(R.id.card_total)
        val cardActive = findViewById<androidx.cardview.widget.CardView>(R.id.card_active)
        val cardCompleted = findViewById<androidx.cardview.widget.CardView>(R.id.card_completed)

        textTotal = cardTotal.findViewById(R.id.text_stat_value)
        textActive = cardActive.findViewById(R.id.text_stat_value)
        textCompleted = cardCompleted.findViewById(R.id.text_stat_value)

        inputSearch = findViewById(R.id.input_search)
        spinnerFilter = findViewById(R.id.spinner_filter)

        recyclerView = findViewById(R.id.recycler_projects)
        adapter = ProjectAdapter(
            emptyList(),
            context = this,
            onDelete = { project -> eliminarProjeto(project) }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val options = listOf("Todos", "Ativos", "Completos")
        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        inputSearch.addTextChangedListener {
            applyFilters()
        }

        lifecycleScope.launch {
            viewModel.projects.collectLatest {
                fullProjectList = it
                applyFilters()
                textTotal.text = viewModel.getTotalCount().toString()
                textActive.text = viewModel.getActiveCount().toString()
                textCompleted.text = viewModel.getCompletedCount().toString()
                Log.d("DEBUG_VIEW", "Lista atualizada: $it")
            }
        }

        viewModel.loadProjects()

        findViewById<Button>(R.id.button_new_project).setOnClickListener {
            val intent = Intent(this, CreateProjectActivity::class.java)
            startActivity(intent)
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
            val matchesStatus = when (selectedStatus.lowercase()) {
                "ativos" -> project.status.equals("active", true)
                "completos" -> project.status.equals("completed", true)
                else -> true // "Todos"
            }
            matchesSearch && matchesStatus
        }

        adapter.updateData(filtered)
    }

    private fun editarProjeto(project: Project) {
        Toast.makeText(this, "Editar: ${project.name}", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarProjeto(project: Project) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Projeto")
            .setMessage("Tens a certeza que queres eliminar '${project.name}'?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    try {
                        viewModel.deleteProject(project.id_project)
                        viewModel.loadProjects()
                        Toast.makeText(this@ManageProjectsActivity, "Projeto eliminado!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@ManageProjectsActivity, "Erro ao eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
