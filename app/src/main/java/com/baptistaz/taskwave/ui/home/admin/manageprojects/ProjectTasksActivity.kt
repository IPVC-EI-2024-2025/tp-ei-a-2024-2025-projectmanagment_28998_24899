package com.baptistaz.taskwave.ui.home.admin.manageprojects

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
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.ui.home.user.TaskAdapter
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ProjectTasksActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonCreateTask: Button
    private lateinit var repository: TaskRepository
    private lateinit var projectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tasks)

        /* toolbar */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Tasks"

        /* id do projecto vindo do intent */
        projectId = intent.getStringExtra("project_id") ?: return
        repository = TaskRepository(RetrofitInstance.taskService)

        /* views */
        recyclerView     = findViewById(R.id.recycler_tasks)
        buttonCreateTask = findViewById(R.id.button_create_task)

        /* adapter */
        adapter = TaskAdapter(
            emptyList(),
            onClick = { task ->
                startActivity(
                    Intent(this, TaskDetailActivity::class.java).putExtra("task", task)
                )
            },
            onDelete = { task ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${task.title}'?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                repository.deleteTask(task.idTask)
                                Toast.makeText(this@ProjectTasksActivity, "Task deleted!", Toast.LENGTH_SHORT).show()
                                loadTasksWithResponsible()
                            } catch (e: Exception) {
                                Toast.makeText(this@ProjectTasksActivity,
                                    "Error deleting: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter       = adapter

        /* primeira carga */
        loadTasksWithResponsible()

        buttonCreateTask.setOnClickListener {
            startActivity(
                Intent(this, CreateTaskActivity::class.java)
                    .putExtra("project_id", projectId)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksWithResponsible()
    }

    private fun loadTasksWithResponsible() {
        lifecycleScope.launch {
            try {
                /* 👇 AGORA enviamos a variável correcta */
                val tasks = repository.getTasksByProject(projectId)

                /* ---- obter responsáveis ---- */
                val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
                val allUserTasks = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }

                val token  = SessionManager.getAccessToken(this@ProjectTasksActivity) ?: ""
                val users  = UserRepository().getAllUsers(token) ?: emptyList()
                val mapId2Name = users.associate { it.id_user to it.name }

                val list = tasks.map { t ->
                    val responsible = allUserTasks
                        .firstOrNull { it.idTask == t.idTask }
                        ?.let { mapId2Name[it.idUser] } ?: "Not assigned"
                    TaskWithUser(t, responsible)
                }

                adapter.updateData(list)

            } catch (e: Exception) {
                Toast.makeText(this@ProjectTasksActivity,
                    "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
