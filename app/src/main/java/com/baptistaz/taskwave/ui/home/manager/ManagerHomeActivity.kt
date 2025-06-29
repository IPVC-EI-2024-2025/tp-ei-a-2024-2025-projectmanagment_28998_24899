package com.baptistaz.taskwave.ui.home.manager

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.ui.home.admin.manageprojects.ProjectAdapter
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ProjectAdapter
    private val projects = mutableListOf<Project>()
    private var managers: List<User> = emptyList()

    private lateinit var projectRepo: ProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_home)

        recycler = findViewById(R.id.recycler_projects)
        val token = SessionManager.getAccessToken(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            adapter = ProjectAdapter(
                projects,
                managers,
                this@ManagerHomeActivity,
                onDelete = { /* opcional: ação de delete */ }
            )
            recycler.layoutManager = LinearLayoutManager(this@ManagerHomeActivity)
            recycler.adapter = adapter

            loadMyProjects()
        }

        findViewById<Button>(R.id.button_logout).setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadMyProjects() {
        val token = SessionManager.getAccessToken(this) ?: return
        projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val managerId = SessionManager.getAuthId(this) ?: return
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = projectRepo.getProjectsByManager(managerId, token)
                projects.clear()
                projects.addAll(result)
                adapter.updateData(result)
            } catch (e: Exception) {
                // Podes mostrar um Toast ou Log aqui
                // Toast.makeText(this@ManagerHomeActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
