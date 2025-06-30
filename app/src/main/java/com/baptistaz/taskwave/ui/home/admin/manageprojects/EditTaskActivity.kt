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

    private lateinit var inputTitle         : EditText
    private lateinit var inputDescription   : EditText
    private lateinit var spinnerState       : Spinner
    private lateinit var inputCreationDate  : EditText
    private lateinit var inputConclusionDate: EditText
    private lateinit var spinnerPriority    : Spinner
    private lateinit var spinnerAssignUser  : Spinner
    private lateinit var buttonSave         : Button
    private lateinit var task               : Task

    private var users : List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Tarefa"

        task = intent.getSerializableExtra("task") as? Task ?: return finish()

        /* refs */
        inputTitle          = findViewById(R.id.input_title)
        inputDescription    = findViewById(R.id.input_description)
        spinnerState        = findViewById(R.id.spinner_state)
        inputCreationDate   = findViewById(R.id.input_creation_date)
        inputConclusionDate = findViewById(R.id.input_conclusion_date)
        spinnerPriority     = findViewById(R.id.spinner_priority)
        spinnerAssignUser   = findViewById(R.id.spinner_assign_user)
        buttonSave          = findViewById(R.id.button_save_edit)

        /* preencher */
        inputTitle.setText(task.title)
        inputDescription.setText(task.description)
        inputCreationDate.setText(task.creationDate)
        inputConclusionDate.setText(task.conclusionDate ?: "")

        spinnerState.adapter    = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("PENDING", "IN_PROGRESS", "COMPLETED"))
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("LOW", "MEDIUM", "HIGH"))
        spinnerState.setSelection((spinnerState.adapter as ArrayAdapter<String>).getPosition(task.state))
        spinnerPriority.setSelection((spinnerPriority.adapter as ArrayAdapter<String>).getPosition(task.priority ?: "LOW"))

        /* carregar users sem Admin */
        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            val all = UserRepository().getAllUsers(token) ?: emptyList()
            users   = all.filter { !it.profileType.equals("ADMIN", true) }

            spinnerAssignUser.adapter = ArrayAdapter(
                this@EditTaskActivity,
                android.R.layout.simple_spinner_dropdown_item,
                users.map { it.name }
            )

            /* seleccionar responsável actual (se existir e não for Admin) */
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val currentUT = utRepo.getUserTasksByTask(task.idTask).firstOrNull()
            currentUT?.let { ut ->
                val idx = users.indexOfFirst { it.id_user == ut.idUser }
                if (idx >= 0) spinnerAssignUser.setSelection(idx)
            }
        }

        buttonSave.setOnClickListener {
            val updated = task.copy(
                title          = inputTitle.text.toString(),
                description    = inputDescription.text.toString(),
                state          = spinnerState.selectedItem.toString(),
                creationDate   = inputCreationDate.text.toString(),
                conclusionDate = inputConclusionDate.text.toString().takeIf { it.isNotBlank() },
                priority       = spinnerPriority.selectedItem.toString()
            )

            val repoTask = TaskRepository(RetrofitInstance.taskService)
            val repoUT   = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    /* update tarefa */
                    repoTask.updateTask(updated.idTask, updated)

                    /* remover associações antigas */
                    repoUT.getUserTasksByTask(updated.idTask).forEach {
                        repoUT.deleteUserTask(it.idUserTask)
                    }

                    /* novo responsável */
                    if (users.isEmpty()) {
                        Toast.makeText(this@EditTaskActivity, "Sem utilizadores elegíveis!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val selUser = users[spinnerAssignUser.selectedItemPosition]
                    val link = UserTask(
                        idUserTask = UUID.randomUUID().toString(),
                        idUser     = selUser.id_user ?: "",
                        idTask     = updated.idTask,
                        registrationDate = null,
                        status     = "ASSIGNED"
                    )
                    repoUT.assignUserToTask(link)

                    Toast.makeText(this@EditTaskActivity, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, intent.putExtra("task", updated))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@EditTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
