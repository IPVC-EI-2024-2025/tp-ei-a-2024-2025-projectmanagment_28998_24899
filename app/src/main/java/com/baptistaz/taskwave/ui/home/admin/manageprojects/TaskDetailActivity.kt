package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var task: Task

    // ActivityResultLauncher para receber a task editada
    private val editTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedTask = result.data?.getSerializableExtra("task") as? Task
            updatedTask?.let {
                task = it
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhe da Tarefa"

        // Recebe a tarefa do intent
        task = intent.getSerializableExtra("task") as Task

        // Mostra info
        updateUI()

        // Clique do botão editar
        findViewById<Button>(R.id.button_edit_task).setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("task", task)
            editTaskLauncher.launch(intent)
        }
    }

    // Atualiza a UI com os dados atuais da tarefa
    private fun updateUI() {
        findViewById<TextView>(R.id.text_title).text = task.title
        findViewById<TextView>(R.id.text_description).text = task.description
        findViewById<TextView>(R.id.text_status).text = task.state
        findViewById<TextView>(R.id.text_creation_date).text = task.creation_date
        findViewById<TextView>(R.id.text_conclusion_date).text = task.conclusion_date ?: ""
        findViewById<TextView>(R.id.text_priority).text = task.priority ?: ""
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
