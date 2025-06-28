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

class EditTaskActivity : AppCompatActivity() {
    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var inputCreationDate: EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonSave: Button
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Tarefa"

        // Recebe a tarefa via intent
        task = intent.getSerializableExtra("task") as Task

        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        spinnerState = findViewById(R.id.spinner_state)
        inputCreationDate = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        buttonSave = findViewById(R.id.button_save_edit)

        // Preenche os campos
        inputTitle.setText(task.title)
        inputDescription.setText(task.description)
        inputCreationDate.setText(task.creation_date)
        inputConclusionDate.setText(task.conclusion_date ?: "")

        val stateOptions = listOf("PENDING", "IN_PROGRESS", "COMPLETED")
        spinnerState.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateOptions)
        spinnerState.setSelection(stateOptions.indexOf(task.state))

        val priorityOptions = listOf("LOW", "MEDIUM", "HIGH")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)
        spinnerPriority.setSelection(priorityOptions.indexOf(task.priority ?: "LOW"))

        buttonSave.setOnClickListener {
            val updatedTask = task.copy(
                title = inputTitle.text.toString(),
                description = inputDescription.text.toString(),
                state = spinnerState.selectedItem.toString(),
                creation_date = inputCreationDate.text.toString(),
                conclusion_date = inputConclusionDate.text.toString().takeIf { it.isNotBlank() },
                priority = spinnerPriority.selectedItem.toString()
            )

            val repository = TaskRepository(RetrofitInstance.taskService)
            lifecycleScope.launch {
                try {
                    repository.updateTask(updatedTask.id_task, updatedTask)
                    Toast.makeText(this@EditTaskActivity, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                    // DEVOLVE a task atualizada
                    val resultIntent = intent
                    resultIntent.putExtra("task", updatedTask)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
