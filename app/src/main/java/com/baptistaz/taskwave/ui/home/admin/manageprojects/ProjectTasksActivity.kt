package com.baptistaz.taskwave.ui.home.admin.manageprojects

import TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ProjectTasksActivity : AppCompatActivity() {

    private lateinit var adapterActive: TaskAdapter
    private lateinit var adapterCompleted: TaskAdapter
    private lateinit var recyclerActive: RecyclerView
    private lateinit var recyclerCompleted: RecyclerView
    private lateinit var buttonCreateTask: Button
    private lateinit var repository: TaskRepository
    private lateinit var projectId: String
    private lateinit var projectStatus: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tasks)

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.tasks)

        // 1) Recupera o ID e o status do projeto vindos da Intent
        projectId    = intent.getStringExtra("project_id") ?: return
        projectStatus = intent.getStringExtra("project_status") ?: "Active"
        val isComplete = projectStatus.equals("Completed", ignoreCase = true)

        // 2) Instancia o repositório
        repository = TaskRepository(RetrofitInstance.taskService)

        // 3) Liga as views
        recyclerActive     = findViewById(R.id.recycler_active)
        recyclerCompleted  = findViewById(R.id.recycler_completed)
        buttonCreateTask   = findViewById(R.id.button_create_task)

        // 4) Se for projeto concluído, esconde o botão de criar
        if (isComplete) buttonCreateTask.visibility = View.GONE

        // 5) Configura os adapters já levando em conta o status
        adapterActive = TaskAdapter(
            emptyList(),
            onClick  = if (isComplete) ({ /* no-op */ }) else { task ->
                startActivity(
                    Intent(this, TaskDetailActivity::class.java)
                        .putExtra("task", task)
                )
            },
            onDelete = if (isComplete) null else { task ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${task.title}'?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                repository.deleteTask(task.idTask)
                                Toast.makeText(
                                    this@ProjectTasksActivity,
                                    "Task deleted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadTasksWithResponsible()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@ProjectTasksActivity,
                                    "Error deleting: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            canEdit  = !isComplete
        )

        adapterCompleted = TaskAdapter(
            emptyList(),
            onClick  = { /* leitura apenas, ou no-op */ },
            onDelete = null,
            canEdit  = false
        )

        // 6) Associa ao RecyclerView
        recyclerActive.layoutManager    = LinearLayoutManager(this)
        recyclerActive.adapter          = adapterActive
        recyclerCompleted.layoutManager = LinearLayoutManager(this)
        recyclerCompleted.adapter       = adapterCompleted

        // 7) Botão de criar tarefa (só aparece se não estiver concluído)
        buttonCreateTask.setOnClickListener {
            startActivity(
                Intent(this, CreateTaskActivity::class.java)
                    .putExtra("project_id", projectId)
            )
        }

        // 8) Carrega as tarefas
        loadTasksWithResponsible()
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
                val token  = SessionManager.getAccessToken(this@ProjectTasksActivity) ?: ""
                val users  = UserRepository().getAllUsers(token) ?: emptyList()
                val mapId2Name = users.associate { it.id_user to it.name }

                // Separar tarefas por estado
                val active = tasks.filter { it.state == "IN_PROGRESS" }
                val completed = tasks.filter { it.state == "COMPLETED" }

                val activeList = active.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }?.let { mapId2Name[it.idUser] } ?: "Not assigned"
                    TaskWithUser(t, responsible)
                }
                val completedList = completed.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }?.let { mapId2Name[it.idUser] } ?: "Not assigned"
                    TaskWithUser(t, responsible)
                }

                adapterActive.updateData(activeList)
                adapterCompleted.updateData(completedList)

            } catch (e: Exception) {
                Toast.makeText(this@ProjectTasksActivity, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
