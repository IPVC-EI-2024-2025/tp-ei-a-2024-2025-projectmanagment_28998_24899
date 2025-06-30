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
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class UserTaskDetailsActivity : AppCompatActivity() {

    private lateinit var adapter: UpdateAdapter
    private lateinit var repoUpd : TaskUpdateRepository
    private lateinit var repoTask: TaskRepository
    private lateinit var taskId  : String

    /* ---------- ActivityResult para voltar a recarregar depois de criar update ---------- */
    private val addUpdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) carregarUpdates()
    }
    /* ----------------------------------------------------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details)

        /* toolbar com seta back */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        repoUpd  = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))
        repoTask = TaskRepository       (RetrofitInstance.taskService)

        adapter = UpdateAdapter(emptyList()) { /* long-click  (editar / apagar) */ }

        findViewById<RecyclerView>(R.id.recycler_updates).apply {
            layoutManager = LinearLayoutManager(this@UserTaskDetailsActivity)
            adapter       = this@UserTaskDetailsActivity.adapter
        }

        /* ---------- FAB “+” para adicionar update ---------- */
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            openAddUpdate()
        }
        /* --------------------------------------------------- */

        carregarUpdates()
    }

    private fun openAddUpdate() {
        val it = Intent(this, AddUpdateActivity::class.java)
        it.putExtra("TASK_ID", taskId)
        addUpdLauncher.launch(it)
    }

    private fun carregarUpdates() = lifecycleScope.launch {
        try {
            val updates = repoUpd.list(taskId).sortedBy { it.date }
            adapter.update(updates)
        } catch (e: Exception) {
            Toast.makeText(this@UserTaskDetailsActivity,
                "Erro ao carregar updates: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
