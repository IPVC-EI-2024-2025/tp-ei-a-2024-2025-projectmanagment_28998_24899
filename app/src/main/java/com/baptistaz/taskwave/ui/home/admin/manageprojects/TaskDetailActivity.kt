package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class TaskDetailActivity : BaseLocalizedActivity() {

    private lateinit var task: Task
    private lateinit var btnDone: Button
    private val repoTask by lazy { TaskRepository(RetrofitInstance.taskService) }

    private val editTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            (res.data?.getSerializableExtra("task") as? Task)?.let {
                task = it
                updateUI()
                toggleDoneButton()
                loadAssignedUser()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        setSupportActionBar(findViewById(R.id.toolbar_task_detail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.task_detail_edit)

        task = intent.getSerializableExtra("task") as Task

        btnDone = findViewById(R.id.button_mark_complete)
        findViewById<Button>(R.id.button_edit_task).setOnClickListener {
            startActivityForEdit()
        }
        btnDone.setOnClickListener { marcarConcluida() }

        updateUI()
        toggleDoneButton()
        loadAssignedUser()
    }

    private fun startActivityForEdit() {
        Intent(this, EditTaskActivity::class.java)
            .putExtra("task", task)
            .also { editTaskLauncher.launch(it) }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.text_title).text = task.title
        findViewById<TextView>(R.id.text_description).text = task.description
        findViewById<TextView>(R.id.text_status).text = task.state
        findViewById<TextView>(R.id.text_creation_date).text = task.creationDate
        findViewById<TextView>(R.id.text_conclusion_date).text = task.conclusionDate ?: ""
        findViewById<TextView>(R.id.text_priority).text = task.priority ?: ""
    }

    private fun toggleDoneButton() {
        btnDone.visibility =
            if (task.state == "IN_PROGRESS") View.VISIBLE else View.GONE
    }

    private fun marcarConcluida() = lifecycleScope.launch {
        try {
            repoTask.markCompleted(task.idTask)
            task = task.copy(state = "COMPLETED")
            updateUI()
            toggleDoneButton()
            Toast.makeText(
                this@TaskDetailActivity,
                getString(R.string.msg_task_completed),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(
                this@TaskDetailActivity,
                "Erro: ${e.message}", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun loadAssignedUser() = lifecycleScope.launch {
        val tv = findViewById<TextView>(R.id.text_assigned_user)
        tv.text = getString(R.string.task_detail_loading)
        try {
            val utRepo = UserTaskRepository(RetrofitInstance.userTaskService)
            val ut = utRepo.getUserTasksByTask(task.idTask).firstOrNull()
            if (ut == null) {
                tv.text = getString(R.string.task_detail_responsible_unassigned)
                return@launch
            }
            val token = SessionManager.getAccessToken(this@TaskDetailActivity) ?: return@launch
            val user = UserRepository().getAllUsers(token)?.find { it.id_user == ut.idUser }
            tv.text = getString(
                R.string.task_detail_responsible_named,
                user?.name ?: getString(R.string.task_detail_responsible_unknown)
            )
        } catch (e: Exception) {
            tv.text = getString(R.string.task_detail_loading_error)
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
