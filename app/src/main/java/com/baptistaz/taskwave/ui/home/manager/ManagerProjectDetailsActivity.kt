package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
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

    private var isReadOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_details)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isReadOnly = intent.getBooleanExtra("READ_ONLY", false)

        textName = findViewById(R.id.text_project_name)
        textDesc = findViewById(R.id.text_project_description)
        textStatus = findViewById(R.id.text_project_status)
        textManager = findViewById(R.id.text_manager)
        textStart = findViewById(R.id.text_project_start)
        textEnd = findViewById(R.id.text_project_end)
        buttonViewTasks = findViewById(R.id.button_view_tasks)

        // Recebe o ID do projeto pelo Intent
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return

        lifecycleScope.launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = repo.getProjectById(projectId)
            if (project != null) {
                textName.text = project.name
                textDesc.text = project.description
                textStatus.text = project.status
                textStart.text = project.startDate
                textEnd.text = project.endDate

                // Buscar manager associado
                val managerId = project.idManager ?: ""
                val manager = if (managerId.isNotEmpty()) {
                    UserRepository().getUserById(managerId, token)
                } else null
                val managerName = manager?.name ?: "No manager"
                textManager.text = "Manager: $managerName"

                buttonViewTasks.setOnClickListener {
                    lifecycleScope.launch {
                        val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
                        val project = repo.getProjectById(projectId)
                        val myId = SessionManager.getUserId(this@ManagerProjectDetailsActivity)
                        val isManager = project?.idManager == myId
                        val intent = Intent(this@ManagerProjectDetailsActivity, ManagerProjectTasksActivity::class.java)
                        intent.putExtra("PROJECT_ID", projectId)
                        intent.putExtra("READ_ONLY", !isManager)
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(this@ManagerProjectDetailsActivity, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
