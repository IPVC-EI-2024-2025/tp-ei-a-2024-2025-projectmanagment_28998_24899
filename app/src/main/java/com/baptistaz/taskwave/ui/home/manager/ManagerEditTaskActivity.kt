package com.baptistaz.taskwave.ui.home.manager

import User
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.UUID

class ManagerEditTaskActivity : BaseLocalizedActivity() {

    private lateinit var inputTitle       : EditText
    private lateinit var inputDescription : EditText
    private lateinit var inputCreation    : EditText
    private lateinit var inputConclusion  : EditText
    private lateinit var spinnerPriority  : Spinner
    private lateinit var spinnerAssign    : Spinner
    private lateinit var txtState         : TextView
    private lateinit var buttonSave       : Button
    private lateinit var task             : Task

    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        setSupportActionBar(findViewById(R.id.toolbar_edit_task))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Editar Tarefa"

        task = intent.getSerializableExtra("task") as? Task ?: return finish()

        /* refs */
        inputTitle       = findViewById(R.id.input_title)
        inputDescription = findViewById(R.id.input_description)
        inputCreation    = findViewById(R.id.input_creation_date)
        inputConclusion  = findViewById(R.id.input_conclusion_date)
        spinnerPriority  = findViewById(R.id.spinner_priority)
        spinnerAssign    = findViewById(R.id.spinner_assign_user)
        txtState         = findViewById(R.id.text_state)
        buttonSave       = findViewById(R.id.button_save_edit)

        txtState.text = "Estado atual: ${task.state}"

        inputTitle.setText(task.title)
        inputDescription.setText(task.description)
        inputCreation.setText(task.creationDate)
        inputConclusion.setText(task.conclusionDate ?: "")

        spinnerPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("LOW", "MEDIUM", "HIGH")
        )
        spinnerPriority.setSelection(
            (spinnerPriority.adapter as ArrayAdapter<String>)
                .getPosition(task.priority ?: "LOW")
        )

        // Carrega users (sem Admin)
        val token = SessionManager.getAccessToken(this) ?: ""
        lifecycleScope.launch {
            users = (UserRepository().getAllUsers(token) ?: emptyList())
                .filterNot { it.profileType.equals("ADMIN", true) }

            spinnerAssign.adapter = ArrayAdapter(
                this@ManagerEditTaskActivity,
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
            val upd = task.copy(
                title          = inputTitle.text.toString(),
                description    = inputDescription.text.toString(),
                creationDate   = inputCreation.text.toString(),
                conclusionDate = inputConclusion.text.toString().takeIf { it.isNotBlank() },
                priority       = spinnerPriority.selectedItem.toString()
            )

            val tRepo  = TaskRepository(RetrofitInstance.taskService)
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)

            lifecycleScope.launch {
                try {
                    tRepo.updateTask(upd.idTask, upd)

                    // Limpa antigos responsáveis
                    utRepo.getUserTasksByTask(upd.idTask)
                        .forEach { utRepo.deleteUserTask(it.idUserTask) }

                    // Novo responsável
                    val sel = users[spinnerAssign.selectedItemPosition]
                    utRepo.assignUserToTask(
                        UserTask(
                            idUserTask = UUID.randomUUID().toString(),
                            idUser     = sel.id_user ?: "",
                            idTask     = upd.idTask,
                            registrationDate = null,
                            status     = "ASSIGNED"
                        )
                    )

                    Toast.makeText(this@ManagerEditTaskActivity, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK, intent.putExtra("task", upd))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ManagerEditTaskActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
