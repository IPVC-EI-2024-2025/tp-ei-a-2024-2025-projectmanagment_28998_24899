package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerHomeActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ManagerProjectAdapter
    private lateinit var projectRepo: ProjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_projects)

        rv = findViewById(R.id.rvProjects)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ManagerProjectAdapter { project ->
            // Abrir detalhe do projeto (depois criamos esta activity)
            Toast.makeText(this, "Clicou em: ${project.name}", Toast.LENGTH_SHORT).show()
            // Exemplo para depois:
            // val intent = Intent(this, ManagerProjectDetailActivity::class.java)
            // intent.putExtra("EXTRA_PROJECT_ID", project.idProject)
            // startActivity(intent)
        }
        rv.adapter = adapter

        // Agora liga aos projetos REAIS do gestor autenticado!
        loadProjectsApi()

        // Botão de logout funcional
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            SessionManager.clearAccessToken(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
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
                    this@ManagerHomeActivity,
                    "Erro ao carregar projetos: ${e.message ?: "desconhecido"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


}
