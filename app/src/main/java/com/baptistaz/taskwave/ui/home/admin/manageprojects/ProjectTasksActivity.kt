package com.baptistaz.taskwave.ui.home.admin.manageprojects

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
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
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

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Tasks"

        projectId = intent.getStringExtra("project_id") ?: return
        repository = TaskRepository(RetrofitInstance.taskService)

        recyclerView = findViewById(R.id.recycler_tasks)
        buttonCreateTask = findViewById(R.id.button_create_task)

        adapter = TaskAdapter(
            emptyList(),
            onClick = { selectedTask ->
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task", selectedTask)
                startActivity(intent)
            },
            onDelete = { selectedTask ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${selectedTask.title}'?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                repository.deleteTask(selectedTask.idTask)
                                Toast.makeText(this@ProjectTasksActivity, "Task deleted!", Toast.LENGTH_SHORT).show()
                                loadTasksWithResponsible()
                            } catch (e: Exception) {
                                Toast.makeText(this@ProjectTasksActivity, "Error deleting: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadTasksWithResponsible()

        buttonCreateTask.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("project_id", projectId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksWithResponsible()
    }

    private fun loadTasksWithResponsible() {
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksByProject("eq.$projectId")

                // 1. Get all usertasks for these tasks
                val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)
                val allUserTasks = mutableListOf<com.baptistaz.taskwave.data.model.UserTask>()
                for (task in tasks) {
                    allUserTasks.addAll(userTaskRepo.getUserTasksByTask(task.idTask))
                }

                // 2. Get all users
                val token = SessionManager.getAccessToken(this@ProjectTasksActivity) ?: ""
                val userRepo = UserRepository()
                val allUsers = userRepo.getAllUsers(token) ?: emptyList()

                // 3. Map user id to name
                val mapUserIdToName = allUsers.associateBy({ it.id_user }, { it.name })

                // 4. Map task id to responsible name (first found user)
                val mapTaskIdToUserName = allUserTasks.associate { ut ->
                    ut.idTask to (mapUserIdToName[ut.idUser] ?: "N/A")
                }

                // 5. Create list TaskWithUser
                val tasksWithUsers = tasks.map { task ->
                    TaskWithUser(task, mapTaskIdToUserName[task.idTask] ?: "Not assigned")
                }

                adapter.updateData(tasksWithUsers)
            } catch (e: Exception) {
                Toast.makeText(this@ProjectTasksActivity, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
