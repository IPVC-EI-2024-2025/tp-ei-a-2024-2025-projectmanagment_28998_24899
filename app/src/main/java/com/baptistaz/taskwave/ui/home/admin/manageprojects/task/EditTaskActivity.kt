package com.baptistaz.taskwave.ui.home.admin.manageprojects.task

import com.baptistaz.taskwave.data.model.auth.User
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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

class EditTaskActivity : BaseLocalizedActivity() {

    private lateinit var inputTitle: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputCreation: EditText
    private lateinit var inputConclusion: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerAssign: Spinner
    private lateinit var txtState: TextView
    private lateinit var buttonSave: Button
    private lateinit var task: Task

    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        setSupportActionBar(findViewById(R.id.toolbar_edit_task))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_task_toolbar_title)

        task = intent.getSerializableExtra("task") as? Task ?: return finish()

        inputTitle = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        inputCreation = findViewById(R.id.input_creation_date)
        inputConclusion = findViewById(R.id.input_conclusion_date)
        spinnerPriority = findViewById(R.id.spinner_priority)
        spinnerAssign = findViewById(R.id.spinner_assign_user)
        txtState = findViewById(R.id.text_state)
        buttonSave = findViewById(R.id.button_save_edit)

        txtState.text = getString(R.string.edit_task_state_prefix, task.state)

        inputTitle.setText(task.title)
        inputDescription.setText(task.description)
        inputCreation.setText(task.creationDate)
        inputConclusion.setText(task.conclusionDate ?: "")

        // 👉 Ativar calendários
        inputCreation.setOnClickListener {
            showDatePicker(inputCreation.text.toString()) { selected ->
                inputCreation.setText(selected)
            }
        }

        inputConclusion.setOnClickListener {
            showDatePicker(inputConclusion.text.toString()) { selected ->
                inputConclusion.setText(selected)
            }
        }

        spinnerPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("LOW", "MEDIUM", "HIGH")
        )
        spinnerPriority.setSelection(
            (spinnerPriority.adapter as ArrayAdapter<String>)
                .getPosition(task.priority ?: "LOW")
        )

        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            users = (UserRepository().getAllUsers(token) ?: emptyList())
                .filter { it.profileType.equals("USER", true) }


            spinnerAssign.adapter = ArrayAdapter(
                this@EditTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                users.map { it.name }
            )

            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            utRepo.getUserTasksByTask(task.idTask).firstOrNull()?.let { ut ->
                val idx = users.indexOfFirst { it.id_user == ut.idUser }
                if (idx >= 0) spinnerAssign.setSelection(idx)
            }
        }

        buttonSave.setOnClickListener {
            val updated = task.copy(
                title = inputTitle.text.toString(),
                description = inputDescription.text.toString(),
                creationDate = inputCreation.text.toString(),
                conclusionDate = inputConclusion.text.toString().takeIf { it.isNotBlank() },
                priority = spinnerPriority.selectedItem.toString()
            )

            val tRepo = TaskRepository(RetrofitInstance.taskService)
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    tRepo.updateTask(updated.idTask, updated)

                    utRepo.getUserTasksByTask(updated.idTask)
                        .forEach { utRepo.deleteUserTask(it.idUserTask) }

                    val selectedUser = users[spinnerAssign.selectedItemPosition]
                    utRepo.assignUserToTask(
                        UserTask(
                            idUserTask = UUID.randomUUID().toString(),
                            idUser = selectedUser.id_user ?: "",
                            idTask = updated.idTask,
                            registrationDate = null,
                            status = "ASSIGNED"
                        )
                    )

                    Toast.makeText(
                        this@EditTaskActivity,
                        getString(R.string.edit_task_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK, intent.putExtra("task", updated))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@EditTaskActivity,
                        getString(R.string.edit_task_error, e.message ?: "Erro desconhecido"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
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

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val formatted = "%04d-%02d-%02d".format(y, m + 1, d)
            onDateSelected(formatted)
        }, year, month, day)

        datePicker.show()
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
