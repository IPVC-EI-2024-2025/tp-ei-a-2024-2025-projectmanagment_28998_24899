package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerProjectsAreaActivity : BaseManagerBottomNavActivity() {

    override fun getSelectedMenuId() = R.id.nav_manager_area

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ManagerProjectAdapter
    private lateinit var projectRepo: ProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_projects)

        rv = findViewById(R.id.rvProjects)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ManagerProjectAdapter { project ->
            // Abrir detalhe do projeto
            val intent = Intent(this, ManagerProjectDetailsActivity::class.java)
            intent.putExtra("PROJECT_ID", project.idProject)
            startActivity(intent)
        }
        rv.adapter = adapter

        loadProjectsApi()
        // (opcional: botão de logout só nesta área, mas melhor por no perfil/definições)
    }

    private fun loadProjectsApi() {
        val token = SessionManager.getAccessToken(this) ?: ""
        val managerId = SessionManager.getUserId(this) ?: ""
        projectRepo = ProjectRepository(
            RetrofitInstance.getProjectService(token)
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val list = projectRepo.getProjectsByManager(managerId)
                adapter.updateData(list)
            } catch (e: Exception) {
                Toast.makeText(
                    this@ManagerProjectsAreaActivity,
                    "Erro ao carregar projetos: ${e.message ?: "desconhecido"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
