package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserDetailsActivity : AppCompatActivity() {

    // UI refs
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvDescContent: TextView

    private lateinit var sectionTasks: View
    private lateinit var sectionProjects: View
    private lateinit var tvTasksAssigned: TextView
    private lateinit var tvTasksCompleted: TextView
    private lateinit var progressTasks: ProgressBar
    private lateinit var listUserProjects: LinearLayout

    private val userRepo = UserRepository()
    private lateinit var userTaskRepo: UserTaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details_activity)

        // Toolbar com back
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Bind views ao layout atual
        tvName           = findViewById(R.id.text_name)
        tvEmail          = findViewById(R.id.text_email)
        tvRole           = findViewById(R.id.text_role)
        tvDesc           = findViewById(R.id.text_description)
        tvDescContent    = findViewById(R.id.text_description_content)

        sectionTasks     = findViewById(R.id.section_tasks)
        sectionProjects  = findViewById(R.id.section_projects)
        tvTasksAssigned  = findViewById(R.id.text_tasks_assigned)
        tvTasksCompleted = findViewById(R.id.text_tasks_completed)
        progressTasks    = findViewById(R.id.progress_tasks)
        listUserProjects = findViewById(R.id.list_user_projects)

        // Lógica de carregamento
        val userId = intent.getStringExtra("userId") ?: return
        val token  = SessionManager.getAccessToken(this) ?: return

        userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))

        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepo.getUserById(userId, token)
            user?.let { populateHeader(it) }
            if (user?.profileType?.equals("ADMIN", true) == true) {
                showAdminOnly()
            } else {
                loadParticipation(userId, token)
            }
        }
    }

    private fun populateHeader(user: User) {
        tvName.text  = user.name
        tvEmail.text = user.email

        // Atualiza a cor/background do badge consoante o role!
        when (user.profileType.uppercase()) {
            "ADMIN" -> {
                tvRole.setBackgroundResource(R.drawable.role_badge_admin)
                tvRole.text = "ADMIN"
            }
            "GESTOR" -> {
                tvRole.setBackgroundResource(R.drawable.role_badge_manager) // <--- azul!
                tvRole.text = "GESTOR"
            }
            else -> {
                tvRole.setBackgroundResource(R.drawable.role_badge_user)
                tvRole.text = "USER"
            }
        }

        supportActionBar?.title = user.name

        if (user.profileType.equals("ADMIN", true)) {
            tvDesc.text = "Has full permissions within the system.\nCan manage projects, users, tasks and export statistics."
            tvDescContent.text = "Descrição detalhada do utilizador/admin."
        } else if (user.profileType.equals("GESTOR", true)) {
            tvDesc.text = "Description"
            tvDescContent.text = "Manages assigned projects. Responsible for creating tasks, distributing work among users, and tracking project progress. Has permissions to evaluate team performance and export relevant statistics."
        } else {
            tvDesc.text = "This user actively participates in projects by completing assigned tasks..."
            tvDescContent.text = "Descrição detalhada do utilizador/admin."
        }
    }

    private fun showAdminOnly() {
        sectionTasks.visibility = View.GONE
        sectionProjects.visibility = View.GONE
    }

    private suspend fun loadParticipation(userId: String, token: String) {
        sectionTasks.visibility = View.VISIBLE
        sectionProjects.visibility = View.VISIBLE

        val tasks: List<UserTask> = userTaskRepo.getTasksOfUser(userId, token) ?: emptyList()
        val taskProjects = tasks.mapNotNull { it.task?.project }.distinctBy { it.idProject }.toMutableList()

        // Busca todos os projetos onde este user é manager!
        val projectsRepo = com.baptistaz.taskwave.data.remote.project.ProjectRepository(
            com.baptistaz.taskwave.data.remote.RetrofitInstance.getProjectService(token)
        )
        val allProjects = projectsRepo.getAllProjects()
        val managedProjects = allProjects.filter { it.idManager == userId }

        // Junta (sem duplicar)
        for (proj in managedProjects) {
            if (taskProjects.none { it.idProject == proj.idProject }) {
                taskProjects.add(proj)
            }
        }

        // Limpa e adiciona no UI
        listUserProjects.removeAllViews()
        for (proj in taskProjects) {
            val text = TextView(this).apply {
                text = proj.name
                textSize = 15f
                setPadding(8, 8, 8, 8)
                setTextColor(getColor(R.color.button_orange))
            }
            listUserProjects.addView(text)
        }
    }


    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
