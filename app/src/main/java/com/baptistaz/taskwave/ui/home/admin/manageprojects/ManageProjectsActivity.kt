package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageProjectsActivity : AppCompatActivity() {

    private lateinit var textTotal: TextView
    private lateinit var textActive: TextView
    private lateinit var textCompleted: TextView

    private val viewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory(
            ProjectRepository(RetrofitInstance.getProjectService(""))
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_projects)

        // Toolbar com botão de voltar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Projects"

        // Inicializa views
        val cardTotal = findViewById<androidx.cardview.widget.CardView>(R.id.card_total)
        val cardActive = findViewById<androidx.cardview.widget.CardView>(R.id.card_active)
        val cardCompleted = findViewById<androidx.cardview.widget.CardView>(R.id.card_completed)

        textTotal = cardTotal.findViewById(R.id.text_stat_value)
        textActive = cardActive.findViewById(R.id.text_stat_value)
        textCompleted = cardCompleted.findViewById(R.id.text_stat_value)


        // Observa os projetos
        lifecycleScope.launch {
            viewModel.projects.collectLatest {
                textTotal.text = viewModel.getTotalCount().toString()
                textActive.text = viewModel.getActiveCount().toString()
                textCompleted.text = viewModel.getCompletedCount().toString()
            }
        }

        // Carrega os projetos
        viewModel.loadProjects()

        findViewById<Button>(R.id.button_new_project).setOnClickListener {
            val intent = Intent(this, CreateProjectActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
