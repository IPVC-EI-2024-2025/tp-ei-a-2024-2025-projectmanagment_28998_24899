package com.baptistaz.taskwave.ui.home.admin.manageprojects.project

import com.baptistaz.taskwave.ui.home.admin.manageprojects.task.TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskWithUser
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.ui.home.admin.manageprojects.task.CreateTaskActivity
import com.baptistaz.taskwave.ui.home.admin.manageprojects.task.TaskDetailActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ProjectTasksActivity : BaseLocalizedActivity() {

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

        setSupportActionBar(findViewById(R.id.toolbar_tasks))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.project_tasks_create)

        projectId = intent.getStringExtra("project_id") ?: return
        projectStatus = intent.getStringExtra("project_status") ?: "Active"
        val isComplete = projectStatus.equals("Completed", ignoreCase = true)

        repository = TaskRepository(RetrofitInstance.taskService)

        recyclerActive = findViewById(R.id.recycler_active)
        recyclerCompleted = findViewById(R.id.recycler_completed)
        buttonCreateTask = findViewById(R.id.button_create_task)

        if (isComplete) buttonCreateTask.visibility = View.GONE

        adapterActive = TaskAdapter(
            emptyList(),
            onClick = if (isComplete) ({ }) else { task ->
                startActivity(
                    Intent(this, TaskDetailActivity::class.java)
                        .putExtra("task", task)
                )
            },
            onDelete = if (isComplete) null else { task ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.project_tasks_delete_title))
                    .setMessage(getString(R.string.project_tasks_delete_confirm, task.title))
                    .setPositiveButton(getString(R.string.delete_project_confirm_yes)) { _, _ ->
                        lifecycleScope.launch {
                            try {
                                repository.deleteTask(task.idTask)
                                Toast.makeText(
                                    this@ProjectTasksActivity,
                                    getString(R.string.project_tasks_delete_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadTasksWithResponsible()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@ProjectTasksActivity,
                                    getString(R.string.project_tasks_delete_error, e.message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.delete_project_confirm_no), null)
                    .show()
            },
            canEdit = !isComplete
        )

        adapterCompleted = TaskAdapter(
            emptyList(),
            onClick = { },
            onDelete = null,
            canEdit = false
        )

        recyclerActive.layoutManager = LinearLayoutManager(this)
        recyclerActive.adapter = adapterActive
        recyclerCompleted.layoutManager = LinearLayoutManager(this)
        recyclerCompleted.adapter = adapterCompleted

        buttonCreateTask.setOnClickListener {
            startActivity(
                Intent(this, CreateTaskActivity::class.java)
                    .putExtra("project_id", projectId)
            )
        }

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

                val token = SessionManager.getAccessToken(this@ProjectTasksActivity) ?: ""
                val users = UserRepository().getAllUsers(token) ?: emptyList()
                val mapId2Name = users.associate { it.id_user to it.name }

                val active = tasks.filter { it.state == "IN_PROGRESS" }
                val completed = tasks.filter { it.state == "COMPLETED" }

                val activeList = active.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }
                        ?.let { mapId2Name[it.idUser] }
                        ?: getString(R.string.project_tasks_not_assigned)
                    TaskWithUser(t, responsible)
                }

                val completedList = completed.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }
                        ?.let { mapId2Name[it.idUser] }
                        ?: getString(R.string.project_tasks_not_assigned)
                    TaskWithUser(t, responsible)
                }

                adapterActive.updateData(activeList)
                adapterCompleted.updateData(completedList)

            } catch (e: Exception) {
                Toast.makeText(
                    this@ProjectTasksActivity,
                    getString(R.string.project_tasks_loading_error, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
