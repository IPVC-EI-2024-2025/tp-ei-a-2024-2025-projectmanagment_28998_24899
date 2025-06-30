package com.baptistaz.taskwave.ui.home.user

import TaskAdapter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserProjectTasksActivity : AppCompatActivity() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var textProjectName: TextView
    private lateinit var adapter: TaskAdapter // ou outro adapter simples só para visualização

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_project_tasks)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        recyclerTasks = findViewById(R.id.recycler_tasks)
        textProjectName = findViewById(R.id.text_project_name)

        recyclerTasks.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(emptyList(), onClick = {}, onDelete = null)
        recyclerTasks.adapter = adapter

        val projectId = intent.getStringExtra("PROJECT_ID") ?: return
        val token = SessionManager.getAccessToken(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            // Opcional: mostra nome do projeto no topo
            val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
            val project = projectRepo.getProjectById(projectId)
            textProjectName.text = project?.name ?: ""

            // Busca tasks do projeto
            val repo = TaskRepository(RetrofitInstance.taskService)
            val tasks = repo.getTasksByProject("eq.$projectId")
            // Faz o mapping para TaskWithUser se quiseres mostrar o responsável
            val userTaskRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val allUserTasks = tasks.flatMap { userTaskRepo.getUserTasksByTask(it.idTask) }

            val userRepo = UserRepository()
            val allUsers = userRepo.getAllUsers(token) ?: emptyList()
            val mapUserIdToName = allUsers.associate { it.id_user to it.name }
            val mapTaskIdToUserName = allUserTasks.associate { ut ->
                ut.idTask to (mapUserIdToName[ut.idUser] ?: "N/A")
            }
            val tasksWithUsers = tasks.map { task ->
                TaskWithUser(task, mapTaskIdToUserName[task.idTask] ?: "Not assigned")
            }

            adapter.updateData(tasksWithUsers)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
