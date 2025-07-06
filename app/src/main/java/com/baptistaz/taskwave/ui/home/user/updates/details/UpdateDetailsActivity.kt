package com.baptistaz.taskwave.ui.home.user.updates.details

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.TaskUpdateRepository
import com.baptistaz.taskwave.ui.home.user.updates.EditUpdateActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Displays the details of a specific task update.
 * Allows the user to edit or delete the update.
 */
class UpdateDetailsActivity : BaseLocalizedActivity() {

    private lateinit var repo: TaskUpdateRepository
    private lateinit var upd: TaskUpdate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar_update_detail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.update_details)

        // Retrieve update object and repository
        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        bind(upd)
        initButtons()
    }

    override fun onResume() {
        super.onResume()
        // Refresh update info in case of edits
        lifecycleScope.launch {
            upd = repo.getById(upd.idUpdate!!)
            bind(upd)
            setResult(RESULT_OK)
        }
    }

    /** Populates the UI with update details */
    private fun bind(u: TaskUpdate) = with(u) {
        findViewById<TextView>(R.id.text_title).text = title
        findViewById<TextView>(R.id.text_date).text = date?.take(10) ?: ""
        findViewById<TextView>(R.id.text_notes).text = notes ?: "-"
        findViewById<TextView>(R.id.text_loc).text = location ?: "-"
        findViewById<TextView>(R.id.text_time).text = timeSpent ?: "-"
    }

    /** Initializes the edit and delete buttons */
    private fun initButtons() {
        // Edit button
        findViewById<MaterialButton>(R.id.btn_edit).apply {
            text = getString(R.string.btn_edit_update)
            setOnClickListener {
                startActivity(
                    Intent(this@UpdateDetailsActivity, EditUpdateActivity::class.java)
                        .putExtra("UPDATE", upd)
                )
            }
        }

        // Delete button
        findViewById<MaterialButton>(R.id.btn_delete).apply {
            text = getString(R.string.btn_delete)
            setOnClickListener {
                lifecycleScope.launch {
                    repo.delete(upd.idUpdate!!)
                    Toast.makeText(
                        this@UpdateDetailsActivity,
                        getString(R.string.toast_update_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
