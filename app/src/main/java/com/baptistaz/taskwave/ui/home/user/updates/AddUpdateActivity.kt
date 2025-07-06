package com.baptistaz.taskwave.ui.home.user.updates

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.task.TaskUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.TaskUpdateRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddUpdateActivity : BaseLocalizedActivity() {

    private lateinit var repo: TaskUpdateRepository
    private lateinit var taskId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_update)

        setSupportActionBar(findViewById(R.id.toolbar_add_update))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        val inputTitle = findViewById<TextInputEditText>(R.id.input_title)
        val inputNotes = findViewById<TextInputEditText>(R.id.input_notes)
        val inputLoc   = findViewById<TextInputEditText>(R.id.input_location)
        val inputTime  = findViewById<TextInputEditText>(R.id.input_time)

        findViewById<Button>(R.id.button_add_update).setOnClickListener {
            val newUpd = TaskUpdate(
                idTask = taskId,
                title = inputTitle.text.toString(),
                notes = inputNotes.text?.toString(),
                location = inputLoc.text?.toString(),
                timeSpent = inputTime.text?.toString()
            )
            lifecycleScope.launch {
                try {
                    repo.create(newUpd)
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AddUpdateActivity,
                        getString(R.string.error_creating_update, e.message ?: "desconhecido"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
