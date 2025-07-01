package com.baptistaz.taskwave.ui.home.user

import TaskAdapter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
    private lateinit var adapter: TaskAdapter

    private lateinit var projectId: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_project_tasks)

        /* toolbar */
        findViewById<Toolbar>(R.id.toolbar).also {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.tasks)
        }

        /* ids vindos do intent / sessão */
        projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        token     = SessionManager.getAccessToken(this) ?: return finish()

        /* UI refs */
        recyclerTasks   = findViewById(R.id.recycler_tasks)
        textProjectName = findViewById(R.id.text_project_name)

        /* adapter: onClick devolve um Task */
        adapter = TaskAdapter(
            emptyList(),
            onClick  = { /* intencionalmente vazio: não abre detalhe */ },
            onDelete = null           // utilizador não pode apagar
        )
        recyclerTasks.layoutManager = LinearLayoutManager(this)
        recyclerTasks.adapter       = adapter

        carregarDados()
    }

    /* --------- network + binding --------- */
    private fun carregarDados() = CoroutineScope(Dispatchers.Main).launch {
        /* nome do projecto */
        val projRepo   = ProjectRepository(RetrofitInstance.getProjectService(token))
        val project    = projRepo.getProjectById(projectId)
        textProjectName.text = project?.name ?: ""

        /* tarefas do projecto */
        val taskRepo   = TaskRepository(RetrofitInstance.taskService)
        val tasks      = taskRepo.getTasksByProject(projectId)          // já só «projectId»
        val utRepo     = UserTaskRepository(RetrofitInstance.userTaskService)
        val allUT      = tasks.flatMap { utRepo.getUserTasksByTask(it.idTask) }

        /* mapa userId → nome */
        val users      = UserRepository().getAllUsers(token) ?: emptyList()
        val mapIdName  = users.associate { it.id_user to it.name }

        /* lista final */
        val listTwu = tasks.map { t ->
            val respName = allUT.firstOrNull { it.idTask == t.idTask }?.let {
                mapIdName[it.idUser]
            } ?: getString(R.string.not_assigned)
            TaskWithUser(t, respName)
        }

        adapter.updateData(listTwu)
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
