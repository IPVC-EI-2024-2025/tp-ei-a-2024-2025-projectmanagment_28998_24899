package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var inputCreationDate: EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonCreate: Button

    private lateinit var projectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova Tarefa"

        // Recebe o id do projeto via intent
        projectId = intent.getStringExtra("project_id") ?: run {
            Toast.makeText(this, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Liga componentes
        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        spinnerState = findViewById(R.id.spinner_state)
        inputCreationDate = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        buttonCreate = findViewById(R.id.button_create_task)

        // Adapta spinners
        val stateOptions = listOf("PENDING", "IN_PROGRESS", "COMPLETED")
        spinnerState.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateOptions)

        val priorityOptions = listOf("LOW", "MEDIUM", "HIGH")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)

        // Botão Criar
        buttonCreate.setOnClickListener {
            val title = inputTitle.text.toString().trim()
            val description = inputDescription.text.toString().trim()
            val state = spinnerState.selectedItem.toString()
            val creationDate = inputCreationDate.text.toString().trim()
            val conclusionDate = inputConclusionDate.text.toString().trim().takeIf { it.isNotBlank() }
            val priority = spinnerPriority.selectedItem.toString()

            if (title.isEmpty() || creationDate.isEmpty()) {
                Toast.makeText(this, "Preenche pelo menos o título e a data de criação!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTask = Task(
                id_task = UUID.randomUUID().toString(),
                id_project = projectId,
                title = title,
                description = description,
                state = state,
                creation_date = creationDate,
                conclusion_date = conclusionDate,
                priority = priority
            )

            val repository = TaskRepository(RetrofitInstance.taskService)
            lifecycleScope.launch {
                try {
                    repository.createTask(newTask)
                    Toast.makeText(this@CreateTaskActivity, "Tarefa criada!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@CreateTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
