package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
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
        recyclerView = findViewById(R.id.recycler_projects)
        adapter = ProjectAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        inputSearch.addTextChangedListener {
            updateFilteredList(it.toString())
        }

        lifecycleScope.launch {
            viewModel.projects.collectLatest {
                fullProjectList = it
                updateFilteredList(inputSearch.text.toString())
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

    private fun updateFilteredList(query: String) {
        val filtered = if (query.isBlank()) {
            fullProjectList
        } else {
            fullProjectList.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filtered)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
