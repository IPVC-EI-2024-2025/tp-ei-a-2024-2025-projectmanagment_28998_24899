package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var task: Task

    // ActivityResultLauncher para receber a task editada
    private val editTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedTask = result.data?.getSerializableExtra("task") as? Task
            updatedTask?.let {
                task = it
                updateUI()
                loadAssignedUser() // <-- Atualiza o responsável depois de editar!
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhe da Tarefa"

        // Recebe a tarefa do intent
        task = intent.getSerializableExtra("task") as Task

        // Mostra info
        updateUI()
        loadAssignedUser()

        // Clique do botão editar
        findViewById<Button>(R.id.button_edit_task).setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("task", task)
            editTaskLauncher.launch(intent)
        }
    }

    // Atualiza a UI com os dados atuais da tarefa
    private fun updateUI() {
        findViewById<TextView>(R.id.text_title).text = task.title
        findViewById<TextView>(R.id.text_description).text = task.description
        findViewById<TextView>(R.id.text_status).text = task.state
        findViewById<TextView>(R.id.text_creation_date).text = task.creationDate
        findViewById<TextView>(R.id.text_conclusion_date).text = task.conclusionDate ?: ""
        findViewById<TextView>(R.id.text_priority).text = task.priority ?: ""
    }

    // Busca e mostra o nome do responsável
    private fun loadAssignedUser() {
        val assignedUserTv = findViewById<TextView>(R.id.text_assigned_user)
        assignedUserTv.text = "Responsável: (a carregar...)"

        lifecycleScope.launch {
            try {
                // 1. Buscar o UserTask (associação tarefa-user)
                val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)
                val userTasks = userTaskRepo.getUserTasksByTask(task.idTask)
                val firstUserTask = userTasks.firstOrNull()

                if (firstUserTask != null) {
                    // 2. Buscar o User associado
                    val token = SessionManager.getAccessToken(this@TaskDetailActivity) ?: ""
                    val userRepo = UserRepository()
                    val users = userRepo.getAllUsers(token) ?: emptyList()
                    val user = users.find { it.id_user == firstUserTask.idUser }

                    if (user != null) {
                        assignedUserTv.text = "Responsável: ${user.name}"
                    } else {
                        assignedUserTv.text = "Responsável: [Utilizador não encontrado]"
                    }
                } else {
                    assignedUserTv.text = "Responsável: Não atribuído"
                }
            } catch (e: Exception) {
                assignedUserTv.text = "Responsável: Erro ao buscar"
                Toast.makeText(this@TaskDetailActivity, "Erro ao carregar responsável", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
