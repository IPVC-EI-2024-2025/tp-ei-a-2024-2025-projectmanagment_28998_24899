package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
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
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.UUID

class EditTaskActivity : AppCompatActivity() {
    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var inputCreationDate: EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerAssignUser: Spinner
    private lateinit var buttonSave: Button
    private lateinit var task: Task

    private var userList: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Tarefa"

        // Recebe a tarefa via intent
        task = intent.getSerializableExtra("task") as Task

        // Liga componentes
        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        spinnerState = findViewById(R.id.spinner_state)
        inputCreationDate = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        spinnerAssignUser = findViewById(R.id.spinner_assign_user)
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

        // Carrega users (coroutine)
        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            val repo = UserRepository()
            userList = repo.getAllUsers(token) ?: emptyList()
            val userNames = userList.map { it.name }
            spinnerAssignUser.adapter = ArrayAdapter(
                this@EditTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                userNames
            )

            // Pré-seleciona user atual se quiseres (extra)
            val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val currentUserTask = userTaskRepo.getUserTasksByTask(task.id_task).firstOrNull()
            currentUserTask?.let { ut ->
                val pos = userList.indexOfFirst { it.id_user == ut.id_user }
                if (pos >= 0) spinnerAssignUser.setSelection(pos)
            }
        }

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
            val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    repository.updateTask(updatedTask.id_task, updatedTask)

                    // Remove associações antigas (se garantires só 1 user por tarefa)
                    val userTasks = userTaskRepo.getUserTasksByTask(updatedTask.id_task)
                    userTasks.forEach { userTaskRepo.deleteUserTask(it.id_usertask) }

                    val selectedUser = userList[spinnerAssignUser.selectedItemPosition]
                    val userId = selectedUser.id_user
                    if (userId == null) {
                        Toast.makeText(this@EditTaskActivity, "Erro: utilizador sem ID!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val newUserTask = UserTask(
                        id_usertask = UUID.randomUUID().toString(),
                        id_user = userId,
                        id_task = updatedTask.id_task,
                        registration_date = null,
                        status = "ASSIGNED"
                    )

                    userTaskRepo.assignUserToTask(newUserTask)
                    Toast.makeText(this@EditTaskActivity, "Tarefa atualizada e utilizador atribuído!", Toast.LENGTH_SHORT).show()
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
