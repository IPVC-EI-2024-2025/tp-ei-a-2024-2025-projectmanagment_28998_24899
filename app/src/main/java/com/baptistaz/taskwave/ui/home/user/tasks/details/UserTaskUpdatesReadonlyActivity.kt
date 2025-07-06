package com.baptistaz.taskwave.ui.home.user.tasks.details

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.TaskUpdateRepository
import com.baptistaz.taskwave.ui.home.user.UpdateAdapter
import com.baptistaz.taskwave.ui.home.user.updates.details.UpdateDetailsReadonlyActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays a read-only list of task updates for the user.
 * Does not allow editing or adding new updates.
 */

class UserTaskUpdatesReadonlyActivity : BaseLocalizedActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UpdateAdapter
    private lateinit var repo: TaskUpdateRepository
    private lateinit var taskId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar_updates))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.task_updates)

        // Get task ID and access token
        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        // Initialize repository
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        // Set up RecyclerView
        recycler = findViewById(R.id.recycler_updates)
        recycler.layoutManager = LinearLayoutManager(this)

        // Adapter with disabled footer (no "+" button)
        adapter = UpdateAdapter(
            mutableListOf(),
            onFooterClick = {},
            onItemClick = { showDetails(it) },
            showFooter = false
        )
        recycler.adapter = adapter

        loadUpdates()
    }

    /** Fetches and displays updates for the task */
    private fun loadUpdates() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val updates = repo.list(taskId).sortedBy { it.date }
            adapter.setData(updates)
        } catch (e: Exception) {
            Toast.makeText(
                this@UserTaskUpdatesReadonlyActivity,
                "Failed to load updates: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Opens the read-only view of a selected update */
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
