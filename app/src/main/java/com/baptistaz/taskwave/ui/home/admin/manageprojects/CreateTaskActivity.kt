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

    private lateinit var inputTitle         : EditText
    private lateinit var inputDescription   : EditText
    private lateinit var spinnerState       : Spinner
    private lateinit var inputCreationDate  : EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority    : Spinner
    private lateinit var spinnerAssignUser  : Spinner
    private lateinit var buttonCreate       : Button

    private lateinit var projectId : String
    private var users : List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        /* Toolbar */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nova Tarefa"

        /* id do projecto */
        projectId = intent.getStringExtra("project_id") ?: run {
            Toast.makeText(this, "Projeto não encontrado!", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        /* refs */
        inputTitle          = findViewById(R.id.input_title)
        inputDescription    = findViewById(R.id.input_description)
        spinnerState        = findViewById(R.id.spinner_state)
        inputCreationDate   = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority     = findViewById(R.id.spinner_priority)
        spinnerAssignUser   = findViewById(R.id.spinner_assign_user)
        buttonCreate        = findViewById(R.id.button_create_task)

        /* spinners fixos */
        spinnerState.adapter    = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("PENDING", "IN_PROGRESS", "COMPLETED"))
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("LOW", "MEDIUM", "HIGH"))

        /* carrega utilizadores (exclui ADMIN) */
        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            val allUsers   = UserRepository().getAllUsers(token) ?: emptyList()
            users          = allUsers.filter { !it.profileType.equals("ADMIN", true) }
            val names      = users.map { it.name }
            spinnerAssignUser.adapter = ArrayAdapter(
                this@CreateTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        /* criar tarefa */
        buttonCreate.setOnClickListener {
            val title   = inputTitle.text.toString().trim()
            val created = inputCreationDate.text.toString().trim()

            if (title.isBlank() || created.isBlank()) {
                Toast.makeText(this, "Título e data de criação são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (users.isEmpty() || spinnerAssignUser.selectedItemPosition == Spinner.INVALID_POSITION) {
                Toast.makeText(this, "Nenhum utilizador elegível (Admins não contam).", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val taskId = UUID.randomUUID().toString()
            val task = Task(
                idTask         = taskId,
                idProject      = projectId,
                title          = title,
                description    = inputDescription.text.toString(),
                state          = spinnerState.selectedItem.toString(),
                creationDate   = created,
                conclusionDate = inputConclusionDate.text.toString().takeIf { it.isNotBlank() },
                priority       = spinnerPriority.selectedItem.toString()
            )

            val assignee = users[spinnerAssignUser.selectedItemPosition]
            val userTask = UserTask(
                idUserTask = UUID.randomUUID().toString(),
                idUser     = assignee.id_user ?: "",
                idTask     = taskId,
                registrationDate = null,
                status     = "ASSIGNED"
            )

            val repoTask = TaskRepository(RetrofitInstance.taskService)
            val repoUT   = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    repoTask.createTask(task)
                    repoUT.assignUserToTask(userTask)
                    Toast.makeText(this@CreateTaskActivity, "Tarefa criada!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@CreateTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
