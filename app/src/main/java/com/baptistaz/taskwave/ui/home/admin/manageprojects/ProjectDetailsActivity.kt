package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var textName: TextView
    private lateinit var textDescription: TextView
    private lateinit var textStatus: TextView
    private lateinit var textStartDate: TextView
    private lateinit var textEndDate: TextView
    private lateinit var buttonViewTasks: Button
    private lateinit var buttonManageManager: Button
    private lateinit var buttonMarkComplete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Details"

        textName = findViewById(R.id.text_project_name)
        textDescription = findViewById(R.id.text_project_description)
        textStatus = findViewById(R.id.text_project_status)
        textStartDate = findViewById(R.id.text_project_start)
        textEndDate = findViewById(R.id.text_project_end)
        buttonViewTasks = findViewById(R.id.button_view_tasks)
        buttonManageManager = findViewById(R.id.button_manage_manager)
        buttonMarkComplete = findViewById(R.id.button_mark_complete)

        // Recebe o Project pelo intent
        val project = intent.getSerializableExtra("project") as? Project
        project?.let {
            textName.text = it.name
            textDescription.text = it.description
            textStatus.text = it.status
            textStartDate.text = it.startDate
            textEndDate.text = it.endDate
        }

        buttonViewTasks.setOnClickListener {
            // Lógica: abrir página de tarefas do projeto
            val intent = Intent(this, ProjectTasksActivity::class.java)
            intent.putExtra("project_id", project?.idProject)
            startActivity(intent)
        }

        buttonManageManager.setOnClickListener {
            // Em breve!
        }

        buttonMarkComplete.setOnClickListener {
            // Em breve!
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
