package com.baptistaz.taskwave.ui.home.admin.manageprojects.task

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

class CreateTaskActivity : BaseLocalizedActivity() {

    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputCreationDate: EditText
    private lateinit var inputConclusion: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerAssignUser: Spinner
    private lateinit var buttonCreate: Button

    private lateinit var projectId: String
    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_task_title)

        projectId = intent.getStringExtra("project_id") ?: return finish()

        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        inputCreationDate = findViewById(R.id.input_creation_date)
        inputConclusion = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        spinnerAssignUser = findViewById(R.id.spinner_assign_user)
        buttonCreate = findViewById(R.id.button_create_task)

        // Ativar DatePicker nos campos de data
        inputCreationDate.setOnClickListener {
            showDatePicker { selectedDate ->
                inputCreationDate.setText(selectedDate)
            }
        }

        inputConclusion.setOnClickListener {
            showDatePicker { selectedDate ->
                inputConclusion.setText(selectedDate)
            }
        }

        spinnerPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("LOW", "MEDIUM", "HIGH")
        )

        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            users = (UserRepository().getAllUsers(token) ?: emptyList())
                .filterNot {
                    it.profileType.equals("ADMIN", true) ||
                            it.profileType.equals("GESTOR", true) ||
                            it.profileType.equals("MANAGER", true)
                }

            spinnerAssignUser.adapter = ArrayAdapter(
                this@CreateTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                users.map { it.name }
            )
        }

        buttonCreate.setOnClickListener {
            val title = inputTitle.text.toString().trim()
            val created = inputCreationDate.text.toString().trim()

            if (title.isBlank() || created.isBlank()) {
                toast(getString(R.string.create_task_missing_fields))
                return@setOnClickListener
            }

            if (users.isEmpty() || spinnerAssignUser.selectedItemPosition == Spinner.INVALID_POSITION) {
                toast(getString(R.string.create_task_no_user_selected))
                return@setOnClickListener
            }

            val taskId = UUID.randomUUID().toString()
            val task = Task(
                idTask = taskId,
                idProject = projectId,
                title = title,
                description = inputDescription.text.toString(),
                state = "IN_PROGRESS",
                creationDate = created,
                conclusionDate = inputConclusion.text.toString().takeIf { it.isNotBlank() },
                priority = spinnerPriority.selectedItem.toString()
            )

            val assignee = users[spinnerAssignUser.selectedItemPosition]
            val link = UserTask(
                idUserTask = UUID.randomUUID().toString(),
                idUser = assignee.id_user ?: "",
                idTask = taskId,
                registrationDate = null,
                status = "ASSIGNED"
            )

            val tRepo = TaskRepository(RetrofitInstance.taskService)
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    tRepo.createTask(task)
                    utRepo.assignUserToTask(link)
                    toast(getString(R.string.create_task_success))
                    finish()
                } catch (e: Exception) {
                    toast(getString(R.string.create_task_error, e.message ?: "Erro desconhecido"))
                }
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val formatted = "%04d-%02d-%02d".format(y, m + 1, d) // AAAA-MM-DD
            onDateSelected(formatted)
        }, year, month, day)

        datePicker.show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
