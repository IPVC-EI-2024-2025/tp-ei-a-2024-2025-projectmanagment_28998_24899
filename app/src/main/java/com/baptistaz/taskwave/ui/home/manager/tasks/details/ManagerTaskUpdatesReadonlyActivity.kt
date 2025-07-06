package com.baptistaz.taskwave.ui.home.manager.tasks.details

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
 * Read-only screen that displays all updates for a specific task.
 * Used by managers to review task progress without editing.
 */
class ManagerTaskUpdatesReadonlyActivity : BaseLocalizedActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: UpdateAdapter
    private lateinit var repo: TaskUpdateRepository
    private lateinit var taskId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_task_details)

        setSupportActionBar(findViewById(R.id.toolbar_updates))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.task_updates)

        // Get task ID from intent
        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        // Setup RecyclerView
        recycler = findViewById(R.id.recycler_updates)
        recycler.layoutManager = LinearLayoutManager(this)

        // Setup adapter in read-only mode (no "+" footer)
        adapter = UpdateAdapter(
            mutableListOf(),
            onFooterClick = {},            // Disabled
            onItemClick = { showDetails(it) }, // Open update details
            showFooter = false             // Hide "+" button
        )
        recycler.adapter = adapter

        loadUpdates()
    }

    /**
     * Loads and displays the updates sorted by date.
     */
    private fun loadUpdates() = CoroutineScope(Dispatchers.Main).launch {
        try {
            val updates = repo.list(taskId).sortedBy { it.date }
            adapter.setData(updates)
        } catch (e: Exception) {
            Toast.makeText(this@ManagerTaskUpdatesReadonlyActivity, "Failed to load updates: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Opens the update in a read-only detail screen.
     */
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
