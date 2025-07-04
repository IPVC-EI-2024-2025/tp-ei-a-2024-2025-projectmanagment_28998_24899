package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Task
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.TaskRepository
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.data.remote.project.UserTaskRepository
import com.baptistaz.taskwave.ui.home.user.UpdateAdapter
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ManagerTaskDetailsActivity : BaseLocalizedActivity() {

    private lateinit var textTitle: TextView
    private lateinit var textDescription: TextView
    private lateinit var textState: TextView
    private lateinit var textCreationDate: TextView
    private lateinit var textConclusionDate: TextView
    private lateinit var textPriority: TextView
    private lateinit var textResponsible: TextView
    private lateinit var textNoUpdates: TextView
    private lateinit var recyclerUpdates: RecyclerView
    private lateinit var btnEdit: Button
    private lateinit var btnDone: Button

    private lateinit var repoUpd : TaskUpdateRepository
    private lateinit var repoTask: TaskRepository
    private lateinit var taskId : String
    private lateinit var task   : Task

    private var canEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_task_details)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textTitle         = findViewById(R.id.text_title)
        textDescription   = findViewById(R.id.text_description)
        textState         = findViewById(R.id.text_state)
        textCreationDate  = findViewById(R.id.text_creation_date)
        textConclusionDate= findViewById(R.id.text_conclusion_date)
        textPriority      = findViewById(R.id.text_priority)
        textResponsible   = findViewById(R.id.text_responsible)
        textNoUpdates     = findViewById(R.id.text_no_updates)
        recyclerUpdates   = findViewById(R.id.recycler_updates)
        btnEdit           = findViewById(R.id.button_edit_task)
        btnDone           = findViewById(R.id.button_mark_done)

        taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        canEdit = intent.getBooleanExtra("CAN_EDIT", false)

        val token = SessionManager.getAccessToken(this) ?: return finish()
        val myUserId = SessionManager.getUserId(this) ?: ""

        repoUpd  = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))
        repoTask = TaskRepository(RetrofitInstance.taskService)

        recyclerUpdates.layoutManager = LinearLayoutManager(this)

        btnEdit.setOnClickListener { openEditTask() }
        btnDone.setOnClickListener { marcarConcluida() }

        if (!canEdit) {
            btnEdit.visibility = View.GONE
            btnDone.visibility = View.GONE
        }

        refreshTaskAndUpdates()
    }

    override fun onResume() {
        super.onResume()
        refreshTaskAndUpdates()
    }

    private fun refreshTaskAndUpdates() = lifecycleScope.launch {
        try {
            task = repoTask.getTaskById(taskId) ?: return@launch

            textTitle.text         = task.title
            textDescription.text   = task.description
            textState.text         = task.state
            textCreationDate.text  = task.creationDate
            textConclusionDate.text= task.conclusionDate ?: "-"
            textPriority.text      = task.priority ?: "-"

            val userTaskRepo = UserTaskRepository(RetrofitInstance.getUserTaskService(token = SessionManager.getAccessToken(this@ManagerTaskDetailsActivity) ?: ""))
            val userTasks = userTaskRepo.getUserTasksByTask(task.idTask)

            val responsibleUserId = userTasks.firstOrNull()?.idUser
            val responsibleName = if (responsibleUserId != null) {
                val userRepo = UserRepository()
                val token = SessionManager.getAccessToken(this@ManagerTaskDetailsActivity) ?: ""
                val user = userRepo.getUserById(responsibleUserId, token)
                user?.name ?: getString(R.string.task_responsible_default)
            } else {
                getString(R.string.task_responsible_default)
            }

            textResponsible.text = getString(R.string.task_responsible_prefix, responsibleName)

            val updates = repoUpd.list(taskId).sortedBy { it.date }

            if (updates.isEmpty()) {
                textNoUpdates.visibility = View.VISIBLE
                recyclerUpdates.visibility = View.GONE
            } else {
                textNoUpdates.visibility = View.GONE
                recyclerUpdates.visibility = View.VISIBLE
                recyclerUpdates.adapter = UpdateAdapter(
                    updates.toMutableList(),
                    onFooterClick = {},
                    onItemClick = {},
                    showFooter = false
                )
            }

            btnDone.visibility = if (canEdit && task.state == "IN_PROGRESS") View.VISIBLE else View.GONE

        } catch (e: Exception) {
            toast("Erro: ${e.message}")
        }
    }

    private fun openEditTask() {
        if (!canEdit) return
        val intent = Intent(this, ManagerEditTaskActivity::class.java)
        intent.putExtra("task", task)
        startActivity(intent)
    }

    private fun marcarConcluida() = lifecycleScope.launch {
        if (!canEdit) return@launch
        repoTask.markCompleted(taskId)
        toast(getString(R.string.toast_task_completed))
        setResult(RESULT_OK)
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
