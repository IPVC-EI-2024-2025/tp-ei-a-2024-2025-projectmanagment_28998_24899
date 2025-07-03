package com.baptistaz.taskwave.ui.home.manager

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

class ManagerCreateTaskActivity : AppCompatActivity() {

    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonCreate: Button
    private lateinit var spinnerResponsible: Spinner

    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_create_task)

        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        buttonCreate = findViewById(R.id.button_create_task)
        spinnerResponsible = findViewById(R.id.spinner_responsible)

        val priorities = listOf("LOW", "MEDIUM", "HIGH")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return

        // Carrega users para o spinner
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            users = UserRepository().getAllUsers(token) ?: emptyList()
            // Filtrar só utilizadores normais (profileType == "USER")
            val normalUsers = users.filter { it.profileType.equals("USER", ignoreCase = true) }
            users = normalUsers // Só guardas os normais!

            val names = users.map { it.name }
            spinnerResponsible.adapter = ArrayAdapter(
                this@ManagerCreateTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        buttonCreate.setOnClickListener {
            val title = inputTitle.text.toString()
            val desc = inputDescription.text.toString()
            val conclusionDate = inputConclusionDate.text.toString()
            val priority = spinnerPriority.selectedItem.toString()

            val selectedUser = users.getOrNull(spinnerResponsible.selectedItemPosition)
            val idResponsible = selectedUser?.id_user

            if (idResponsible == null) {
                Toast.makeText(this, "Escolhe um responsável!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = Task(
                idTask = UUID.randomUUID().toString(),
                idProject = projectId,
                title = title,
                description = desc,
                state = "IN_PROGRESS",
                creationDate = getCurrentDate(),
                conclusionDate = if (conclusionDate.isNotBlank()) conclusionDate else null,
                priority = priority,
                project = null
            )

            val repo = TaskRepository(RetrofitInstance.taskService)
            val userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))

            lifecycleScope.launch {
                try {
                    repo.createTask(task)
                    val userTask = UserTask(
                        idUserTask = UUID.randomUUID().toString(),
                        idUser = idResponsible,
                        idTask = task.idTask,
                        registrationDate = getCurrentDate(),
                        status = "IN_PROGRESS",
                        task = null
                    )

                    userTaskRepo.assignUserToTask(userTask)
                    Toast.makeText(this@ManagerCreateTaskActivity, "Tarefa criada!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ManagerCreateTaskActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}
