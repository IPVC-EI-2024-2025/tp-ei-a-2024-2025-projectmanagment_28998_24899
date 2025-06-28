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

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var spinnerState: Spinner
    private lateinit var inputCreationDate: EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerAssignUser: Spinner
    private lateinit var buttonCreate: Button

    private lateinit var projectId: String
    private var userList: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Toolbar setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova Tarefa"

        // Obter o id do projeto
        projectId = intent.getStringExtra("project_id") ?: run {
            Toast.makeText(this, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // Liga componentes
        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        spinnerState = findViewById(R.id.spinner_state)
        inputCreationDate = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        spinnerAssignUser = findViewById(R.id.spinner_assign_user)
        buttonCreate = findViewById(R.id.button_create_task)

        // Setup spinners
        val stateOptions = listOf("PENDING", "IN_PROGRESS", "COMPLETED")
        spinnerState.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateOptions)
        val priorityOptions = listOf("LOW", "MEDIUM", "HIGH")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorityOptions)

        // Carregar utilizadores para o spinner
        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            val repo = UserRepository()
            userList = repo.getAllUsers(token) ?: emptyList()
            val userNames = userList.map { it.name }
            spinnerAssignUser.adapter = ArrayAdapter(
                this@CreateTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                userNames
            )
        }

        // Criar tarefa e associar utilizador
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
            if (userList.isEmpty() || spinnerAssignUser.selectedItemPosition == Spinner.INVALID_POSITION) {
                Toast.makeText(this, "Seleciona um utilizador!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTaskId = UUID.randomUUID().toString()
            val newTask = Task(
                id_task = newTaskId,
                id_project = projectId,
                title = title,
                description = description,
                state = state,
                creation_date = creationDate,
                conclusion_date = conclusionDate,
                priority = priority
            )

            val selectedUser = userList[spinnerAssignUser.selectedItemPosition]
            val userId = selectedUser.id_user
            if (userId.isNullOrBlank()) {
                Toast.makeText(this, "Erro: utilizador sem ID!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userTask = UserTask(
                id_usertask = UUID.randomUUID().toString(),
                id_user = userId,
                id_task = newTaskId,
                registration_date = null,
                status = "ASSIGNED"
            )

            val repository = TaskRepository(RetrofitInstance.taskService)
            val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            lifecycleScope.launch {
                try {
                    repository.createTask(newTask)
                    userTaskRepo.assignUserToTask(userTask)
                    Toast.makeText(this@CreateTaskActivity, "Tarefa criada e atribuída!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@CreateTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }
}
