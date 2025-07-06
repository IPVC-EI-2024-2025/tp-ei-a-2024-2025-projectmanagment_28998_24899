package com.baptistaz.taskwave.ui.home.user.home

import com.baptistaz.taskwave.data.model.auth.User
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.data.remote.project.repository.UserTaskRepository
import com.baptistaz.taskwave.ui.home.user.tasks.TaskHistoryActivity
import com.baptistaz.taskwave.ui.home.user.projects.UserProjectDetailsActivity
import com.baptistaz.taskwave.ui.home.user.tasks.details.UserTaskDetailsActivity
import com.baptistaz.taskwave.ui.home.user.base.BaseBottomNavActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserHomeActivity : BaseBottomNavActivity() {

    override fun getSelectedMenuId() = R.id.nav_home

    private lateinit var layoutProjectsActive: FlexboxLayout
    private lateinit var layoutProjectsCompleted: FlexboxLayout
    private lateinit var layoutTasks: LinearLayout
    private lateinit var imageUser: ImageView
    private lateinit var textGreeting: TextView
    private lateinit var btnHistory: Button
    private lateinit var btnMyEvals: Button

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        layoutProjectsActive = findViewById(R.id.layout_projects_active)
        layoutProjectsCompleted = findViewById(R.id.layout_projects_completed)
        layoutTasks = findViewById(R.id.layout_tasks)
        imageUser = findViewById(R.id.image_user)
        textGreeting = findViewById(R.id.text_greeting)
        btnMyEvals = findViewById(R.id.button_my_evaluations)
        btnHistory = findViewById(R.id.button_history)

        btnHistory.setOnClickListener {
            currentUser?.id_user?.let { id ->
                startActivity(Intent(this, TaskHistoryActivity::class.java).putExtra("USER_ID", id))
            } ?: Toast.makeText(this, getString(R.string.toast_missing_user_id), Toast.LENGTH_SHORT).show()
        }

        btnMyEvals.setOnClickListener {
            startActivity(Intent(this, UserEvaluationsActivity::class.java))
        }

        loadUserDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadUserDashboard()
    }

    private fun loadUserDashboard() {
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val userRepo = UserRepository()
            userRepo.getUserByAuthId(authId, token)?.let { u ->
                currentUser = u
                textGreeting.text = getString(R.string.greeting_user, u.name)
                u.id_user?.let { loadProjectsAndTasks(it, token) }
            }
        }
    }

    private suspend fun loadProjectsAndTasks(userId: String, token: String) {
        val utRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
        val allUserTasks = utRepo.getTasksOfUser(userId, token) ?: emptyList()

        val projects = allUserTasks.mapNotNull { it.task?.project }
            .distinctBy { it.idProject }

        val projetosAtivos = projects.filter { it.status.equals("ACTIVE", true) }
        val projetosConcluidos = projects.filter { it.status.equals("COMPLETED", true) }

        layoutProjectsActive.removeAllViews()
        layoutProjectsCompleted.removeAllViews()

        projetosAtivos.forEach { proj ->
            layoutProjectsActive.addView(createProjectButton(proj))
        }

        projetosConcluidos.forEach { proj ->
            layoutProjectsCompleted.addView(createProjectButton(proj))
        }

        // --- Tarefas em progresso ---
        val activeTasks = allUserTasks
            .filter { it.task?.state == "IN_PROGRESS" }
            .sortedBy { it.task?.conclusionDate ?: "" }

        layoutTasks.removeAllViews()
        activeTasks.forEach { ut ->
            val task = ut.task ?: return@forEach
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.card_bg)
                setPadding(20, 16, 20, 16)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    startActivity(Intent(context, UserTaskDetailsActivity::class.java).putExtra("TASK_ID", task.idTask))
                }
            }

            card.addView(TextView(this).apply {
                text = task.title
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            })

            card.addView(TextView(this).apply {
                text = task.description
            })

            card.addView(TextView(this).apply {
                val deadline = task.conclusionDate ?: getString(R.string.no_date)
                text = "${task.state}\n$deadline"
                setTextColor(ContextCompat.getColor(context, R.color.gray_dark))
                textSize = 14f
            })

            val badge = TextView(this).apply {
                text = task.priority ?: ""
                setPadding(18, 8, 18, 8)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setTypeface(null, Typeface.BOLD)
                textSize = 13f
                background = when ((task.priority ?: "").uppercase()) {
                    "HIGH" -> ContextCompat.getDrawable(context, R.drawable.priority_badge_high)
                    "MEDIUM" -> ContextCompat.getDrawable(context, R.drawable.priority_badge_medium)
                    "LOW" -> ContextCompat.getDrawable(context, R.drawable.priority_badge_low)
                    else -> null
                }
            }
            card.addView(LinearLayout(this).apply { addView(badge) })

            layoutTasks.addView(card)
        }
    }

    private fun createProjectButton(proj: Project): Button {
        return Button(this).apply {
            text = proj.name
            setBackgroundResource(R.drawable.card_bg)
            setTextColor(ContextCompat.getColor(context, R.color.button_orange))
            setOnClickListener {
                startActivity(Intent(context, UserProjectDetailsActivity::class.java).putExtra("PROJECT_ID", proj.idProject))
            }
        }
    }
}
