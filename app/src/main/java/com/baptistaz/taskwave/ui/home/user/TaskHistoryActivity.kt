package com.baptistaz.taskwave.ui.home.user

import TaskAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskWithUser
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class TaskHistoryActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter : TaskAdapter
    private lateinit var token   : String
    private lateinit var userId  : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_history)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history)

        token  = SessionManager.getAccessToken(this) ?: return finish()
        // TaskHistoryActivity.kt  (no onCreate, antes de fazer queries)
        userId = intent.getStringExtra("USER_ID")
            ?: SessionManager.getUserId(this)
                    ?: return finish()       // se mesmo assim não houver ID, sai.


        recycler = findViewById(R.id.recycler_history)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(emptyList(), onClick = { /* detalhes em read-only */ }, onDelete = null)
        recycler.adapter = adapter

        carregarHistorico()
    }

    private fun carregarHistorico() = lifecycleScope.launch {
        try {
            val utRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token))
            val tasks  = utRepo.getTasksOfUser(userId, token)
                ?.mapNotNull { it.task }
                ?.filter { it.state == "COMPLETED" }
                ?.sortedByDescending { it.conclusionDate }    // mais recentes no topo
                ?: emptyList()

            val twu = tasks.map { TaskWithUser(it, /* já não precisamos do responsável aqui */ "") }
            adapter.updateData(twu)
        } catch (e: Exception) {
            Toast.makeText(this@TaskHistoryActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
