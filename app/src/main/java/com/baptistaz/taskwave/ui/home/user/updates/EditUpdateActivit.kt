package com.baptistaz.taskwave.ui.home.user.updates

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.TaskUpdateRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Screen that allows a user to edit a task update.
 */
class EditUpdateActivity : BaseLocalizedActivity() {

    private lateinit var repo: TaskUpdateRepository
    private lateinit var upd : TaskUpdate

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_add_update) // shared layout with AddUpdateActivity

        // Toolbar setup
        setSupportActionBar(findViewById(R.id.toolbar_add_update))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_edit_update)

        // Retrieve update object and setup repository
        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        // Bind input fields
        val tTitle = findViewById<TextInputEditText>(R.id.input_title)
        val tNotes = findViewById<TextInputEditText>(R.id.input_notes)
        val tLoc   = findViewById<TextInputEditText>(R.id.input_location)
        val tTime  = findViewById<TextInputEditText>(R.id.input_time)

        tTitle.setText(upd.title)
        tNotes.setText(upd.notes)
        tLoc.setText(upd.location)
        tTime.setText(upd.timeSpent)

        // Edit button (same ID as "Add" but with different label and behavior)
        findViewById<MaterialButton>(R.id.button_add_update)
            .apply { text = getString(R.string.title_edit_update) }
            .setOnClickListener {
                val edited = upd.copy(
                    title     = tTitle.text.toString(),
                    notes     = tNotes.text?.toString(),
                    location  = tLoc.text?.toString(),
                    timeSpent = tTime.text?.toString()
                )

                // Send update to backend
                lifecycleScope.launch {
                    repo.edit(upd.idUpdate!!, edited)
                    Toast.makeText(
                        this@EditUpdateActivity,
                        getString(R.string.toast_update_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
