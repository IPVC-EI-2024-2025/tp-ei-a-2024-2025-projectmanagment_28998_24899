package com.baptistaz.taskwave.ui.home.user.tasks.details

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.Task
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskUpdateRepository
import com.baptistaz.taskwave.ui.home.user.updates.AddUpdateActivity
import com.baptistaz.taskwave.ui.home.user.UpdateAdapter
import com.baptistaz.taskwave.ui.home.user.updates.details.UpdateDetailsActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class UserTaskDetailsActivity : BaseLocalizedActivity() {

    private lateinit var adapter : UpdateAdapter
    private lateinit var repoUpd : TaskUpdateRepository
    private lateinit var repoTask: TaskRepository

    private lateinit var taskId : String
    private lateinit var btnDone: Button
    private lateinit var task   : Task

    // Launcher para adicionar atualização
    private val addUpdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) refreshUpdates() }

    // Launcher para detalhes de uma atualização (e possível remoção)
    private val detailsUpdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) refreshUpdates() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details)

        /* toolbar */
        setSupportActionBar(findViewById(R.id.toolbar_updates))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* ids */
        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        repoUpd  = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))
        repoTask = TaskRepository       (RetrofitInstance.taskService)

        /* botão */
        btnDone = findViewById(R.id.button_mark_done)
        btnDone.visibility = View.GONE
        btnDone.setOnClickListener { marcarConcluida() }

        /* recycler */
        adapter = UpdateAdapter(
            mutableListOf(),
            onFooterClick = { openAddUpdate() },
            onItemClick   = { openDetails(it) }
        )
        findViewById<RecyclerView>(R.id.recycler_updates).apply {
            layoutManager = LinearLayoutManager(this@UserTaskDetailsActivity)
            adapter       = this@UserTaskDetailsActivity.adapter
        }

        refreshTaskAndUpdates()
    }

    /* ---------- network ---------- */

    private fun refreshTaskAndUpdates() = lifecycleScope.launch {
        try {
            task = repoTask.getTaskById(taskId) ?: return@launch
            btnDone.visibility =
                if (task.state == "IN_PROGRESS") View.VISIBLE else View.GONE
            refreshUpdates()
        } catch (e: Exception) {
            toast("Erro: ${e.message}")
        }
    }

    private fun refreshUpdates() = lifecycleScope.launch {
        try {
            val list = repoUpd.list(taskId).sortedBy { it.date }
            adapter.setData(list)
        } catch (e: Exception) {
            toast(getString(R.string.error_loading_updates, e.message ?: "erro"))
        }
    }

    private fun marcarConcluida() = lifecycleScope.launch {
        try {
            repoTask.markCompleted(taskId)
            toast(getString(R.string.toast_task_completed))
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            toast(getString(R.string.toast_mark_failed, e.message ?: "erro"))
        }
    }

    private fun openAddUpdate() {
        val intent = Intent(this, AddUpdateActivity::class.java)
            .putExtra("TASK_ID", taskId)
        addUpdLauncher.launch(intent)
    }

    private fun openDetails(upd: TaskUpdate) {
        val intent = Intent(this, UpdateDetailsActivity::class.java)
            .putExtra("UPDATE", upd)
            .putExtra("TASK_ID", taskId)
        detailsUpdLauncher.launch(intent)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
