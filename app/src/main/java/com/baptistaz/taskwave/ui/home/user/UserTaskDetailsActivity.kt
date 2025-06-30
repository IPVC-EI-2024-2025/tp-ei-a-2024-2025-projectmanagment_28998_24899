package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class UserTaskDetailsActivity : AppCompatActivity() {

    private lateinit var adapter : UpdateAdapter
    private lateinit var repoUpd : TaskUpdateRepository
    private lateinit var repoTask: TaskRepository
    private lateinit var taskId  : String

    /* volta do Add/Edit para refrescar a lista */
    private val addUpdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) carregarUpdates() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details)

        /* Toolbar ← */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        repoUpd  = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))
        repoTask = TaskRepository       (RetrofitInstance.taskService)

        adapter = UpdateAdapter(
            mutableListOf(),
            onFooterClick = { openAddUpdate() },
            onItemClick   = { upd ->
                val it = Intent(this, UpdateDetailsActivity::class.java)
                it.putExtra("UPDATE", upd)
                it.putExtra("TASK_ID", taskId)
                startActivity(it)
            }
        )

        findViewById<RecyclerView>(R.id.recycler_updates).apply {
            layoutManager = LinearLayoutManager(this@UserTaskDetailsActivity)
            adapter       = this@UserTaskDetailsActivity.adapter
        }

        carregarUpdates()
    }

    /* ---------- abrir formulário de criação ---------- */
    private fun openAddUpdate() {
        val i = Intent(this, AddUpdateActivity::class.java).apply {
            putExtra("TASK_ID", taskId)
        }
        addUpdLauncher.launch(i)
    }

    /* ---------- abrir ecrã de detalhes ---------- */
    private fun openDetails(upd: TaskUpdate) {
        val i = Intent(this, UpdateDetailsActivity::class.java).apply {
            putExtra("UPDATE",   upd)
            putExtra("TASK_ID", taskId)
        }
        startActivity(i)
    }

    /* ---------- carregar / refrescar lista ---------- */
    private fun carregarUpdates() = lifecycleScope.launch {
        try {
            val updates = repoUpd.list(taskId).sortedBy { it.date }
            adapter.setData(updates)
        } catch (e: Exception) {
            Toast.makeText(this@UserTaskDetailsActivity,
                "Erro ao carregar updates: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarUpdates()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
