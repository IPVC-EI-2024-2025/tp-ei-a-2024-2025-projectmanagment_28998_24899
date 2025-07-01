package com.baptistaz.taskwave.ui.home.manager

import TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskWithUser
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ManagerProjectTasksActivity : AppCompatActivity() {

    private lateinit var adapterActive: TaskAdapter
    private lateinit var adapterCompleted: TaskAdapter
    private lateinit var recyclerActive: RecyclerView
    private lateinit var recyclerCompleted: RecyclerView
    private lateinit var buttonCreateTask: Button
    private lateinit var repository: TaskRepository
    private lateinit var projectId: String

    private var isManager: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_tasks)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tarefas"

        projectId = intent.getStringExtra("PROJECT_ID") ?: return
        repository = TaskRepository(RetrofitInstance.taskService)

        recyclerActive = findViewById(R.id.recycler_active)
        recyclerCompleted = findViewById(R.id.recycler_completed)
        buttonCreateTask = findViewById(R.id.button_create_task)

        // ⚠️ SEMPRE BUSCA O PROJETO AO BACKEND PARA SABER SE PODES EDITAR
        val token = SessionManager.getAccessToken(this) ?: return
        val myUserId = SessionManager.getUserId(this) ?: return

        lifecycleScope.launch {
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = projectRepo.getProjectById(projectId)
            isManager = (project?.idManager == myUserId)

            // Adapter para tarefas em progresso (editar/remover)
            adapterActive = TaskAdapter(
                emptyList(),
                onClick = { task ->
                    val intent = Intent(this@ManagerProjectTasksActivity, ManagerTaskDetailsActivity::class.java)
                    intent.putExtra("TASK_ID", task.idTask)
                    intent.putExtra("CAN_EDIT", isManager)
                    startActivity(intent)
                },
                onDelete = { task ->
                    if (isManager) {
                        AlertDialog.Builder(this@ManagerProjectTasksActivity)
                            .setTitle("Eliminar Tarefa")
                            .setMessage("Tens a certeza que queres eliminar '${task.title}'?")
                            .setPositiveButton("Sim") { _, _ ->
                                lifecycleScope.launch {
                                    try {
                                        repository.deleteTask(task.idTask)
                                        Toast.makeText(this@ManagerProjectTasksActivity, "Tarefa eliminada!", Toast.LENGTH_SHORT).show()
                                        loadTasksWithResponsible()
                                    } catch (e: Exception) {
                                        Toast.makeText(this@ManagerProjectTasksActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                },
                canEdit = isManager
            )

            // Adapter para concluídas (apenas visualizar)
            adapterCompleted = TaskAdapter(
                emptyList(),
                onClick = { /* apenas detalhes, se quiseres! */ },
                onDelete = null,
                canEdit = false
            )

            recyclerActive.layoutManager = LinearLayoutManager(this@ManagerProjectTasksActivity)
            recyclerActive.adapter = adapterActive

            recyclerCompleted.layoutManager = LinearLayoutManager(this@ManagerProjectTasksActivity)
            recyclerCompleted.adapter = adapterCompleted

            buttonCreateTask.setOnClickListener {
                if (isManager) {
                    val intent = Intent(this@ManagerProjectTasksActivity, ManagerCreateTaskActivity::class.java)
                    intent.putExtra("PROJECT_ID", projectId)
                    startActivity(intent)
                }
            }
            buttonCreateTask.visibility = if (isManager) Button.VISIBLE else Button.GONE

            loadTasksWithResponsible()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksWithResponsible()
    }

    private fun loadTasksWithResponsible() {
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksByProject(projectId)
                val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
                val allUserTasks = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }
                val token  = SessionManager.getAccessToken(this@ManagerProjectTasksActivity) ?: ""
                val users  = UserRepository().getAllUsers(token) ?: emptyList()
                val mapId2Name = users.associate { it.id_user to it.name }

                // Separar tarefas por estado
                val active = tasks.filter { it.state == "IN_PROGRESS" }
                val completed = tasks.filter { it.state == "COMPLETED" }

                val activeList = active.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }?.let { mapId2Name[it.idUser] } ?: "Não atribuído"
                    TaskWithUser(t, responsible)
                }
                val completedList = completed.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }?.let { mapId2Name[it.idUser] } ?: "Não atribuído"
                    TaskWithUser(t, responsible)
                }

                adapterActive.updateData(activeList)
                adapterCompleted.updateData(completedList)

            } catch (e: Exception) {
                Toast.makeText(this@ManagerProjectTasksActivity, "Erro ao carregar tarefas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
