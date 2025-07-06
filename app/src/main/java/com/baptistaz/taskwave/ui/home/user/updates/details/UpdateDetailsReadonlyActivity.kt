package com.baptistaz.taskwave.ui.home.user.updates.details

import android.os.Bundle
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.utils.BaseLocalizedActivity

/**
 * Read-only screen for viewing the details of a task update.
 * This activity hides the edit and delete buttons from the UI.
 */
class UpdateDetailsReadonlyActivity : BaseLocalizedActivity() {

    private lateinit var upd: TaskUpdate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        // Toolbar setup
        setSupportActionBar(findViewById(R.id.toolbar_update_detail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.update_details)

        // Load the update passed via Intent
        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate

        // Populate UI fields with update data
        bind(upd)

        // Hide edit and delete buttons (read-only mode)
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_edit).visibility = android.view.View.GONE
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_delete).visibility = android.view.View.GONE
    }

    /** Populates the screen with update information */
    private fun bind(u: TaskUpdate) = with(u) {
        findViewById<TextView>(R.id.text_title).text = title
        findViewById<TextView>(R.id.text_date).text = date?.take(10) ?: ""
        findViewById<TextView>(R.id.text_notes).text = notes ?: "-"
        findViewById<TextView>(R.id.text_loc).text = location ?: "-"
        findViewById<TextView>(R.id.text_time).text = timeSpent ?: "-"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
