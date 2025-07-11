package com.baptistaz.taskwave.ui.home.manager.tasks.list

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskWithUser
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageprojects.task.TaskAdapter
import com.baptistaz.taskwave.ui.home.manager.tasks.ManagerCreateTaskActivity
import com.baptistaz.taskwave.ui.home.manager.tasks.details.ManagerTaskDetailsActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Shows all tasks related to a specific project, separated by status (active/completed).
 */
class ManagerProjectTasksActivity : BaseLocalizedActivity() {

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

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_tasks)

        // Arguments
        projectId = intent.getStringExtra("PROJECT_ID") ?: return
        repository = TaskRepository(RetrofitInstance.taskService)

        // Bind views
        recyclerActive = findViewById(R.id.recycler_active)
        recyclerCompleted = findViewById(R.id.recycler_completed)
        buttonCreateTask = findViewById(R.id.button_create_task)

        val token = SessionManager.getAccessToken(this) ?: return
        val myUserId = SessionManager.getUserId(this) ?: return

        // Load project data to check if current user is manager
        lifecycleScope.launch {
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = projectRepo.getProjectById(projectId)
            isManager = (project?.idManager == myUserId)

            // Active tasks adapter
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
                            .setTitle(getString(R.string.dialog_delete_title))
                            .setMessage(getString(R.string.dialog_delete_message, task.title))
                            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                lifecycleScope.launch {
                                    try {
                                        repository.deleteTask(task.idTask)
                                        Toast.makeText(this@ManagerProjectTasksActivity, getString(R.string.task_deleted), Toast.LENGTH_SHORT).show()
                                        loadTasksWithResponsible()
                                    } catch (e: Exception) {
                                        Toast.makeText(this@ManagerProjectTasksActivity, getString(R.string.error_deleting_task, e.message), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show()
                    }
                },
                canEdit = isManager
            )

            // Completed tasks adapter (read-only)
            adapterCompleted = TaskAdapter(
                emptyList(),
                onClick = { task ->
                    val intent = Intent(this@ManagerProjectTasksActivity, ManagerTaskDetailsActivity::class.java)
                    intent.putExtra("TASK_ID", task.idTask)
                    intent.putExtra("CAN_EDIT", false)
                    startActivity(intent)
                },
                onDelete = null,
                canEdit = false
            )

            // Setup recyclers
            recyclerActive.layoutManager = LinearLayoutManager(this@ManagerProjectTasksActivity)
            recyclerActive.adapter = adapterActive

            recyclerCompleted.layoutManager = LinearLayoutManager(this@ManagerProjectTasksActivity)
            recyclerCompleted.adapter = adapterCompleted

            // Create task button
            buttonCreateTask.setOnClickListener {
                if (isManager) {
                    val intent = Intent(this@ManagerProjectTasksActivity, ManagerCreateTaskActivity::class.java)
                    intent.putExtra("PROJECT_ID", projectId)
                    startActivity(intent)
                }
            }
            buttonCreateTask.visibility = if (isManager) Button.VISIBLE else Button.GONE

            // Load task data
            loadTasksWithResponsible()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksWithResponsible()
    }

    /**
     * Loads tasks of the project along with their responsible users
     */
    private fun loadTasksWithResponsible() {
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksByProject(projectId)

                val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
                val allUserTasks = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }

                val token = SessionManager.getAccessToken(this@ManagerProjectTasksActivity) ?: ""
                val users = UserRepository().getAllUsers(token) ?: emptyList()
                val mapId2Name = users.associate { it.id_user to it.name }

                // Separates active and completed
                val active = tasks.filter { it.state == "IN_PROGRESS" }
                val completed = tasks.filter { it.state == "COMPLETED" }

                val activeList = active.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }
                        ?.let { mapId2Name[it.idUser] } ?: getString(R.string.unassigned)
                    TaskWithUser(t, responsible)
                }

                val completedList = completed.map { t ->
                    val responsible = allUserTasks.firstOrNull { it.idTask == t.idTask }
                        ?.let { mapId2Name[it.idUser] } ?: getString(R.string.unassigned)
                    TaskWithUser(t, responsible)
                }

                adapterActive.updateData(activeList)
                adapterCompleted.updateData(completedList)

            } catch (e: Exception) {
                Toast.makeText(this@ManagerProjectTasksActivity, getString(R.string.error_loading_tasks, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
