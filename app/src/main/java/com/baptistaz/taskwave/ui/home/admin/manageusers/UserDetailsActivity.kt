package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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

    /* header */
    private lateinit var tvName : TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole : TextView
    private lateinit var tvDesc : TextView
    private lateinit var tvDescContent : TextView

    /* stats & lists */
    private lateinit var sectionTasks    : View
    private lateinit var sectionProjects : View
    private lateinit var tvAssigned      : TextView
    private lateinit var tvCompleted     : TextView
    private lateinit var progress        : ProgressBar
    private lateinit var listUserTasks   : LinearLayout
    private lateinit var listProjects    : LinearLayout

    private val userRepo = UserRepository()
    private lateinit var userTaskRepo: UserTaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details_activity)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* bind */
        tvName        = findViewById(R.id.text_name)
        tvEmail       = findViewById(R.id.text_email)
        tvRole        = findViewById(R.id.text_role)
        tvDesc        = findViewById(R.id.text_description)
        tvDescContent = findViewById(R.id.text_description_content)

        sectionTasks     = findViewById(R.id.section_tasks)
        sectionProjects  = findViewById(R.id.section_projects)
        tvAssigned       = findViewById(R.id.text_tasks_assigned)
        tvCompleted      = findViewById(R.id.text_tasks_completed)
        progress         = findViewById(R.id.progress_tasks)
        listUserTasks    = findViewById(R.id.list_user_tasks)
        listProjects     = findViewById(R.id.list_user_projects)

        /* ids */
        val userId = intent.getStringExtra("userId") ?: return finish()
        val token  = SessionManager.getAccessToken(this) ?: return finish()

        userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = userRepo.getUserById(userId, token)
                user?.let { populateHeader(it) }
                if (user?.profileType.equals("ADMIN", true)) {
                    showAdminOnly()
                } else {
                    loadParticipation(userId, token)
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserDetailsActivity,
                    "Erro a carregar detalhes: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /* ---------- header ---------- */
    private fun populateHeader(u: User) {
        tvName.text  = u.name
        tvEmail.text = u.email
        supportActionBar?.title = u.name

        when (u.profileType.uppercase()) {
            "ADMIN"  -> { tvRole.text = "ADMIN";  tvRole.setBackgroundResource(R.drawable.role_badge_admin) }
            "GESTOR" -> { tvRole.text = "GESTOR"; tvRole.setBackgroundResource(R.drawable.role_badge_manager) }
            else     -> { tvRole.text = "USER";   tvRole.setBackgroundResource(R.drawable.role_badge_user) }
        }

        when (u.profileType.uppercase()) {
            "ADMIN"  -> {
                tvDesc.text = "Has full permissions within the system."
                tvDescContent.text = "Can manage projects, users, tasks and export statistics."
            }
            "GESTOR" -> {
                tvDesc.text = "Description"
                tvDescContent.text = "Manages assigned projects and distributes work among users."
            }
            else -> {
                tvDesc.text = "This user actively participates in projects by completing assigned tasks..."
                tvDescContent.text = "Descrição detalhada do utilizador."
            }
        }
    }

    private fun showAdminOnly() {
        sectionTasks.visibility    = View.GONE
        sectionProjects.visibility = View.GONE
    }

    /* ---------- tarefas + projectos ---------- */
    private suspend fun loadParticipation(userId: String, token: String) {
        sectionTasks.visibility    = View.VISIBLE
        sectionProjects.visibility = View.VISIBLE

        val tasks: List<UserTask> = userTaskRepo.getTasksOfUser(userId, token) ?: emptyList()

        /* ---- estatísticas ---- */
        val totalAssigned  = tasks.size
        val totalCompleted = tasks.count { it.task?.state.equals("COMPLETED", true) }

        tvAssigned.text  = "Assigned tasks: $totalAssigned"
        tvCompleted.text = "Completed tasks: $totalCompleted"
        progress.max      = totalAssigned.coerceAtLeast(1)
        progress.progress = totalCompleted

        /* ---- lista de tarefas ---- */
        listUserTasks.removeAllViews()
        for (ut in tasks) {
            val t = ut.task ?: continue
            val tv = TextView(this).apply {
                text = "• ${t.title} (${t.state})"
                textSize = 15f
                setPadding(8, 4, 8, 4)
            }
            listUserTasks.addView(tv)
        }

        /* ---- projectos ---- */
        val taskProjects = tasks.mapNotNull { it.task?.project }.distinctBy { it.idProject }.toMutableList()

        /* + projectos onde é gestor */
        val allProjects = com.baptistaz.taskwave.data.remote.project.ProjectRepository(
            RetrofitInstance.getProjectService(token)
        ).getAllProjects()
        val managed = allProjects.filter { it.idManager == userId }
        managed.filterNot { p -> taskProjects.any { it.idProject == p.idProject } }
            .forEach { taskProjects.add(it) }

        listProjects.removeAllViews()
        for (p in taskProjects) {
            val tv = TextView(this).apply {
                text = p.name
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 6, 8, 6)
                setTextColor(getColor(R.color.button_orange))
            }
            listProjects.addView(tv)
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
