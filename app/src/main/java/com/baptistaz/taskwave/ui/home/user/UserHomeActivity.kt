package com.baptistaz.taskwave.ui.home.user

import User
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    private lateinit var layoutProjects: LinearLayout
    private lateinit var layoutTasks: LinearLayout
    private lateinit var imageUser: ImageView
    private lateinit var textGreeting: TextView
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        layoutProjects = findViewById(R.id.layout_projects)
        layoutTasks = findViewById(R.id.layout_tasks)
        imageUser = findViewById(R.id.image_user)
        textGreeting = findViewById(R.id.text_greeting)

        loadUserDashboard()
    }

    private fun loadUserDashboard() {
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val userRepo = UserRepository()
            val user = userRepo.getUserByAuthId(authId, token)
            user?.let { u ->
                currentUser = u
                textGreeting.text = "Hi, ${u.name} 👋"
                u.id_user?.let { userId ->
                    loadProjectsAndTasks(userId, token)
                }
            }
        }
    }

    private suspend fun loadProjectsAndTasks(userId: String, token: String) {
        val userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
        val userTasks: List<UserTask> = userTaskRepo.getTasksOfUser(userId, token) ?: emptyList()

        // Projetos distintos associados ao utilizador
        val projects: List<Project> = userTasks.mapNotNull { it.task?.project }.distinctBy { it.idProject }
        layoutProjects.removeAllViews()
        for (proj in projects) {
            val btn = Button(this).apply {
                text = proj.name
                setBackgroundResource(R.drawable.card_bg)
                setTextColor(getColor(R.color.button_orange))
                setOnClickListener {
                    // Detalhes do projeto
                }
            }
            layoutProjects.addView(btn)
        }

        // Tarefas atribuídas ao utilizador
        layoutTasks.removeAllViews()
        for (taskAssign in userTasks) {
            val task = taskAssign.task
            if (task != null) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.card_bg)
                    setPadding(20, 16, 20, 16)
                }
                val title = TextView(this).apply {
                    text = task.title
                    setTextColor(getColor(R.color.black))
                    textSize = 18f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                val desc = TextView(this).apply {
                    text = task.description
                }
                val info = TextView(this).apply {
                    val state = task.state ?: "No State"
                    val deadline = task.conclusionDate ?: "No Date"
                    text = "$state\n$deadline"
                    setTextColor(getColor(R.color.gray_dark))
                    textSize = 14f
                }
                // Badge de prioridade
                val badge = TextView(this).apply {
                    text = task.priority ?: ""
                    setPadding(18, 8, 18, 8)
                    setTextColor(getColor(android.R.color.white))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    textSize = 13f
                    background = when ((task.priority ?: "").uppercase()) {
                        "HIGH" -> getDrawable(R.drawable.priority_badge_high)
                        "MEDIUM" -> getDrawable(R.drawable.priority_badge_medium)
                        "LOW" -> getDrawable(R.drawable.priority_badge_low)
                        else -> null
                    }
                }
                val badgeLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(badge)
                }
                card.addView(title)
                card.addView(desc)
                card.addView(info)
                card.addView(badgeLayout)
                layoutTasks.addView(card)
            }
        }
    }
}
