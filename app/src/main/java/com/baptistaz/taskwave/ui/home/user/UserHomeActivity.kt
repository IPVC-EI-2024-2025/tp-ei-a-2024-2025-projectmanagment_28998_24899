package com.baptistaz.taskwave.ui.home.user

import User
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
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.model.UserTask
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserHomeActivity : BaseBottomNavActivity() {

    override fun getSelectedMenuId() = R.id.nav_home

    /* UI refs */
    private lateinit var layoutProjects: LinearLayout
    private lateinit var layoutTasks   : LinearLayout
    private lateinit var imageUser     : ImageView
    private lateinit var textGreeting  : TextView
    private lateinit var btnHistory    : Button
    private lateinit var btnMyEvals   : Button

    private var currentUser: User? = null

    /* ---------- lifecycle ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        layoutProjects = findViewById(R.id.layout_projects)
        layoutTasks    = findViewById(R.id.layout_tasks)
        imageUser      = findViewById(R.id.image_user)
        textGreeting   = findViewById(R.id.text_greeting)
        btnMyEvals  = findViewById(R.id.button_my_evaluations)
        btnHistory     = findViewById(R.id.button_history)

        // UserHomeActivity.kt  (dentro do onCreate)
        btnHistory.setOnClickListener {
            currentUser?.id_user?.let { id ->
                Intent(this, TaskHistoryActivity::class.java)
                    .putExtra("USER_ID", id)
                    .also { startActivity(it) }
            } ?: Toast.makeText(this, "Utilizador sem ID!", Toast.LENGTH_SHORT).show()
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

    /* ---------- dashboard ---------- */
    private fun loadUserDashboard() {
        val token  = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this)     ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val userRepo = UserRepository()
            userRepo.getUserByAuthId(authId, token)?.let { u ->
                currentUser = u
                textGreeting.text = getString(R.string.greeting_user, u.name)  // "Hi, %s 👋"
                u.id_user?.let { loadProjectsAndTasks(it, token) }
            }
        }
    }

    private suspend fun loadProjectsAndTasks(userId: String, token: String) {
        val utRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
        val allUserTasks: List<UserTask> =
            utRepo.getTasksOfUser(userId, token) ?: emptyList()

        /* ---------- PROJETOS ---------- */
        val projects: List<Project> =
            allUserTasks.mapNotNull { it.task?.project }
                .distinctBy { it.idProject }

        layoutProjects.removeAllViews()
        projects.forEach { proj ->
            val btn = Button(this).apply {
                text = proj.name
                setBackgroundResource(R.drawable.card_bg)
                setTextColor(ContextCompat.getColor(context, R.color.button_orange))
                setOnClickListener {
                    Intent(context, UserProjectDetailsActivity::class.java)
                        .putExtra("PROJECT_ID", proj.idProject)
                        .also { startActivity(it) }
                }
            }
            layoutProjects.addView(btn)
        }

        /* ---------- TAREFAS (apenas IN_PROGRESS) ---------- */
        val activeTasks = allUserTasks
            .filter { it.task?.state == "IN_PROGRESS" }
            .sortedBy { it.task?.conclusionDate ?: "" }      // YYYY-MM-DD → ordena bem

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
                    Intent(context, UserTaskDetailsActivity::class.java)
                        .putExtra("TASK_ID", task.idTask)
                        .also { startActivity(it) }
                }
            }

            // título
            card.addView(TextView(this).apply {
                text = task.title
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
            })

            // descrição
            card.addView(TextView(this).apply { text = task.description })

            // info
            card.addView(TextView(this).apply {
                val deadline = task.conclusionDate ?: getString(R.string.no_date) // "No Date"
                text = "${task.state}\n$deadline"
                setTextColor(ContextCompat.getColor(context, R.color.gray_dark))
                textSize = 14f
            })

            // badge prioridade
            val badge = TextView(this).apply {
                text = task.priority ?: ""
                setPadding(18, 8, 18, 8)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setTypeface(null, Typeface.BOLD)
                textSize = 13f
                background = when ((task.priority ?: "").uppercase()) {
                    "HIGH"   -> ContextCompat.getDrawable(context, R.drawable.priority_badge_high)
                    "MEDIUM" -> ContextCompat.getDrawable(context, R.drawable.priority_badge_medium)
                    "LOW"    -> ContextCompat.getDrawable(context, R.drawable.priority_badge_low)
                    else     -> null
                }
            }
            card.addView(LinearLayout(this).also { it.addView(badge) })

            layoutTasks.addView(card)
        }
    }
}
