package com.baptistaz.taskwave.ui.home.user.projects

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity that displays the list of tasks associated with a project for the normal user.
 */
class UserProjectTasksActivity : BaseLocalizedActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var textProjectName: TextView
    private lateinit var adapter: TaskAdapter

    private lateinit var projectId: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_project_tasks)

        // Toolbar setup
        findViewById<Toolbar>(R.id.toolbar_project_tasks).also {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.tasks)
        }

        // Retrieve project ID and access token
        projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        token     = SessionManager.getAccessToken(this) ?: return finish()

        // Initialize UI components
        recyclerTasks   = findViewById(R.id.recycler_tasks)
        textProjectName = findViewById(R.id.text_project_name)

        // Set up the adapter (no click or delete action for normal user)
        adapter = TaskAdapter(
            emptyList(),
            onClick  = {},
            onDelete = null
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter       = adapter

        carregarDados()
    }

    /**
     * Loads project and task data from API and updates the UI.
     */
    private fun carregarDados() = CoroutineScope(Dispatchers.Main).launch {
        try {
            // Load project details
            val projRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = projRepo.getProjectById(projectId)
            textProjectName.text = project?.name ?: ""

            // Load all tasks for the project
            val taskRepo = TaskRepository(RetrofitInstance.taskService)
            val tasks = taskRepo.getTasksByProject(projectId)

            // Load task-user assignments for each task
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val allUT = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }

            // Load user list and map IDs to names
            val users = UserRepository().getAllUsers(token) ?: emptyList()
            val mapIdName = users.associate { it.id_user to it.name }

            // Join tasks with their responsible user name
            val listTwu = tasks.map { t ->
                val respName = allUT.firstOrNull { it.idTask == t.idTask }?.let {
                    mapIdName[it.idUser]
                } ?: getString(R.string.not_assigned)
                TaskWithUser(t, respName)
            }

            adapter.updateData(listTwu)
        } catch (e: Exception) {
            Toast.makeText(this@UserProjectTasksActivity,
                getString(R.string.error_loading_tasks, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
