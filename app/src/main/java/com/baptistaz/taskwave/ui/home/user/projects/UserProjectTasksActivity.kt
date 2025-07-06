package com.baptistaz.taskwave.ui.home.user.projects

import com.baptistaz.taskwave.ui.home.admin.manageprojects.task.TaskAdapter
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskWithUser
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProjectTasksActivity : BaseLocalizedActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var textProjectName: TextView
    private lateinit var adapter: TaskAdapter

    private lateinit var projectId: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_project_tasks)

        findViewById<Toolbar>(R.id.toolbar_project_tasks).also {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.tasks)
        }

        projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        token     = SessionManager.getAccessToken(this) ?: return finish()

        recyclerTasks   = findViewById(R.id.recycler_tasks)
        textProjectName = findViewById(R.id.text_project_name)

        adapter = TaskAdapter(
            emptyList(),
            onClick  = {},
            onDelete = null
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter       = adapter

        carregarDados()
    }

    private fun carregarDados() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val projRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = projRepo.getProjectById(projectId)
            textProjectName.text = project?.name ?: ""

            val taskRepo = TaskRepository(RetrofitInstance.taskService)
            val tasks = taskRepo.getTasksByProject(projectId)

            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val allUT = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }

            val users = UserRepository().getAllUsers(token) ?: emptyList()
            val mapIdName = users.associate { it.id_user to it.name }

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
