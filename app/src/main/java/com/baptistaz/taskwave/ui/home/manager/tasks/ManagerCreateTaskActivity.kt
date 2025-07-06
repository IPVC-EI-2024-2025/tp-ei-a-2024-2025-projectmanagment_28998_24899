package com.baptistaz.taskwave.ui.home.manager.tasks

import com.baptistaz.taskwave.data.model.auth.User
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.Task
import com.baptistaz.taskwave.data.model.auth.UserTask
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class ManagerCreateTaskActivity : BaseLocalizedActivity() {

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

        // 👉 Date picker para a data de conclusão
        inputConclusionDate.setOnClickListener {
            showDatePicker(inputConclusionDate.text.toString()) { selected ->
                inputConclusionDate.setText(selected)
            }
        }

        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            users = UserRepository().getAllUsers(token)?.filter {
                it.profileType.equals("USER", ignoreCase = true)
            } ?: emptyList()

            spinnerResponsible.adapter = ArrayAdapter(
                this@ManagerCreateTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                users.map { it.name }
            )
        }

        buttonCreate.text = getString(R.string.btn_create)
        buttonCreate.setOnClickListener {
            val title = inputTitle.text.toString()
            val desc = inputDescription.text.toString()
            val conclusionDate = inputConclusionDate.text.toString()
            val priority = spinnerPriority.selectedItem.toString()

            val selectedUser = users.getOrNull(spinnerResponsible.selectedItemPosition)
            val idResponsible = selectedUser?.id_user

            if (idResponsible == null) {
                Toast.makeText(this, getString(R.string.label_responsible), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@ManagerCreateTaskActivity, getString(R.string.title_create_task) + "!", Toast.LENGTH_SHORT).show()
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

    private fun showDatePicker(initialDate: String?, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        if (!initialDate.isNullOrBlank()) {
            try {
                val parts = initialDate.split("-")
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } catch (_: Exception) {}
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val picker = DatePickerDialog(this, { _, y, m, d ->
            val formatted = "%04d-%02d-%02d".format(y, m + 1, d)
            onDateSelected(formatted)
        }, year, month, day)

        picker.show()
    }
}
