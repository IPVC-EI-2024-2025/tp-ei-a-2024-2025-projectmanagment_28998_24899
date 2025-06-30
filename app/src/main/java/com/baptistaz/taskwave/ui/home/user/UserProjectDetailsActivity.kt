package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProjectDetailsActivity : AppCompatActivity() {

    private lateinit var textName: TextView
    private lateinit var textDescription: TextView
    private lateinit var textStatus: TextView
    private lateinit var textManager: TextView
    private lateinit var textStart: TextView
    private lateinit var textEnd: TextView
    private lateinit var buttonViewTasks: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_project_details)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        textName = findViewById(R.id.text_project_name)
        textDescription = findViewById(R.id.text_project_description)
        textStatus = findViewById(R.id.text_project_status)
        textManager = findViewById(R.id.text_manager)
        textStart = findViewById(R.id.text_project_start)
        textEnd = findViewById(R.id.text_project_end)
        buttonViewTasks = findViewById(R.id.button_view_tasks)

        // Recebe o ID do projeto pelo Intent
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return
        val token = SessionManager.getAccessToken(this) ?: return

        // Busca detalhes do projeto
        CoroutineScope(Dispatchers.Main).launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = repo.getProjectById(projectId)
            if (project != null) {
                textName.text = project.name
                textDescription.text = project.description
                textStatus.text = project.status
                textStart.text = project.startDate
                textEnd.text = project.endDate

                // Buscar apenas o manager associado ao projeto
                val managerId = project.idManager ?: ""
                val manager = if (managerId.isNotEmpty()) {
                    UserRepository().getUserById(managerId, token)
                } else {
                    null
                }
                val managerName = manager?.name ?: "No manager"
                textManager.text = managerName
                Log.d("CurrentManager", "ID=$managerId, Name=$managerName")

                buttonViewTasks.setOnClickListener {
                    val intent = Intent(this@UserProjectDetailsActivity, UserProjectTasksActivity::class.java)
                    intent.putExtra("PROJECT_ID", projectId)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this@UserProjectDetailsActivity, "Project not found!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
