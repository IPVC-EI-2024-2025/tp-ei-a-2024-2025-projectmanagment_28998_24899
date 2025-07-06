package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserTaskUpdatesReadonlyActivity : BaseLocalizedActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UpdateAdapter
    private lateinit var repo: TaskUpdateRepository
    private lateinit var taskId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details) // pode usar o mesmo layout

        setSupportActionBar(findViewById(R.id.toolbar_updates))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.task_updates)

        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        recycler = findViewById(R.id.recycler_updates)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = UpdateAdapter(
            mutableListOf(),
            onFooterClick = {},         // não vai aparecer
            onItemClick = { showDetails(it) },
            showFooter = false          // 💡 não mostra botão "+"
        )

        recycler.adapter = adapter

        loadUpdates()
    }

    private fun loadUpdates() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val updates = repo.list(taskId).sortedBy { it.date }
            adapter.setData(updates)
        } catch (e: Exception) {
            Toast.makeText(this@UserTaskUpdatesReadonlyActivity, "Erro ao carregar updates: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDetails(upd: TaskUpdate) {
        startActivity(Intent(this, UpdateDetailsReadonlyActivity::class.java).apply {
            putExtra("UPDATE", upd)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
