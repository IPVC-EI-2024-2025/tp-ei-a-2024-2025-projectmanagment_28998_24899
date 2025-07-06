package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserDetailsActivity : BaseLocalizedActivity() {

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
        supportActionBar?.title = getString(R.string.title_user_details)

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
                when (user?.profileType?.uppercase()) {
                    "ADMIN" -> {
                        showAdminOnly()
                    }
                    "GESTOR" -> {
                        sectionTasks.visibility = View.GONE
                        sectionProjects.visibility = View.VISIBLE
                        // podes ainda carregar os projetos se quiseres, mas não as tarefas
                        loadProjectsOnly(userId, token)
                    }
                    else -> {
                        loadParticipation(userId, token) // utilizador normal
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@UserDetailsActivity,
                    getString(R.string.project_tasks_loading_error, e.message),
                    Toast.LENGTH_LONG).show()
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
            "ADMIN"  -> {
                tvRole.text = getString(R.string.sample_user_role)
                tvRole.setBackgroundResource(R.drawable.role_badge_admin)
            }
            "GESTOR" -> {
                tvRole.text = getString(R.string.stat_managers)
                tvRole.setBackgroundResource(R.drawable.role_badge_manager)
                sectionTasks.visibility = View.GONE
            }
            else     -> {
                tvRole.text = getString(R.string.stat_users)
                tvRole.setBackgroundResource(R.drawable.role_badge_user)
            }
        }

        // Usar as strings padrão que já estão nos XML
        tvDesc.text = getString(R.string.label_description)

        tvDescContent.text = when (u.profileType.uppercase()) {
            "ADMIN" -> getString(R.string.desc_admin)
            "GESTOR" -> getString(R.string.desc_manager)
            else -> getString(R.string.desc_user)
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

        tvAssigned.text  = getString(R.string.text_tasks_assigned).replace("0", totalAssigned.toString())
        tvCompleted.text = getString(R.string.text_tasks_completed).replace("0", totalCompleted.toString())
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

    private suspend fun loadProjectsOnly(userId: String, token: String) {
        sectionProjects.visibility = View.VISIBLE

        val taskProjects = userTaskRepo.getTasksOfUser(userId, token)
            ?.mapNotNull { it.task?.project }
            ?.distinctBy { it.idProject }
            ?.toMutableList() ?: mutableListOf()

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

}
