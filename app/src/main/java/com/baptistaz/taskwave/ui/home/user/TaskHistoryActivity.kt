package com.baptistaz.taskwave.ui.home.user

import TaskAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskWithUser
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class TaskHistoryActivity : BaseLocalizedActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter : TaskAdapter
    private lateinit var token   : String
    private lateinit var userId  : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_history)

        setSupportActionBar(findViewById(R.id.toolbar_history))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history)

        token = SessionManager.getAccessToken(this) ?: return finish()
        userId = intent.getStringExtra("USER_ID")
            ?: SessionManager.getUserId(this)
                    ?: return finish()

        recycler = findViewById(R.id.recycler_history)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            emptyList(),
            onClick = { task ->
                val intent = Intent(this, UserTaskUpdatesReadonlyActivity::class.java)
                intent.putExtra("TASK_ID", task.idTask)
                startActivity(intent)
            },
            onDelete = null
        )

        recycler.adapter = adapter

        carregarHistorico()
    }

    private fun carregarHistorico() = lifecycleScope.launch {
        try {
            val utRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
            val tasks = utRepo.getTasksOfUser(userId, token)
                ?.mapNotNull { it.task }
                ?.filter { it.state == "COMPLETED" }
                ?.sortedByDescending { it.conclusionDate }
                ?: emptyList()

            val twu = tasks.map { TaskWithUser(it, "") }
            adapter.updateData(twu)
        } catch (e: Exception) {
            Toast.makeText(this@TaskHistoryActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
