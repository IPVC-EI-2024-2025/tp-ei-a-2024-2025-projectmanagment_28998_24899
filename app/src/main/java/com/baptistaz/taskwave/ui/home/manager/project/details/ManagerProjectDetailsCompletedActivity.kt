package com.baptistaz.taskwave.ui.home.manager.project.details

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.manager.repository.EvaluationRepository
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.project.repository.TaskRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.manager.evaluations.EvaluationAdapter
import com.baptistaz.taskwave.ui.home.manager.tasks.details.ManagerTaskUpdatesReadonlyActivity
import com.baptistaz.taskwave.ui.home.manager.tasks.list.ManagerProjectTasksReadOnlyAdapter
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager screen for viewing details of a completed project.
 * Shows project info, tasks and team evaluations.
 */
class ManagerProjectDetailsCompletedActivity : BaseLocalizedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_project_details_completed)

        // Toolbar setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Get project ID from intent
        val projectId = intent.getStringExtra("PROJECT_ID") ?: return finish()
        val token = SessionManager.getAccessToken(this) ?: return finish()

        // Repositories
        val projectRepo = ProjectRepository(RetrofitInstance.getProjectService(token))
        val taskRepo = TaskRepository(RetrofitInstance.getTaskService(token))
        val userRepo = UserRepository()
        val evaluationRepo = EvaluationRepository(token)

        CoroutineScope(Dispatchers.Main).launch {
            // Load project details
            val project = projectRepo.getProjectById(projectId)
            project?.let {
                findViewById<TextView>(R.id.text_project_name).text = it.name
                findViewById<TextView>(R.id.text_project_description).text = it.description ?: ""
                findViewById<TextView>(R.id.text_project_status).text = it.status ?: ""
                findViewById<TextView>(R.id.text_start_date).text = it.startDate ?: ""
                findViewById<TextView>(R.id.text_end_date).text = it.endDate ?: ""

                val managerName = it.idManager?.let { id ->
                    userRepo.getUserById(id, token)?.name
                } ?: getString(R.string.unknown_user)

                findViewById<TextView>(R.id.text_manager).text =
                    getString(R.string.label_manager_with_value, managerName)
            }

            // Load project tasks
            val rvTasks = findViewById<RecyclerView>(R.id.rvProjectTasks)
            rvTasks.layoutManager = LinearLayoutManager(this@ManagerProjectDetailsCompletedActivity)
            val tasks = taskRepo.getTasksByProject(projectId)

            // Adapter for tasks with click listener to view task updates
            val adapter = ManagerProjectTasksReadOnlyAdapter(tasks) { task ->
                openTaskUpdates(task.idTask)
            }
            rvTasks.adapter = adapter

            // Load team evaluations
            val rvEvaluations = findViewById<RecyclerView>(R.id.rvEvaluations)
            rvEvaluations.layoutManager = LinearLayoutManager(this@ManagerProjectDetailsCompletedActivity)

            val evaluations = evaluationRepo.getEvaluationsByProject(projectId)
            val evaluationsWithUserNames = evaluations.map { eval ->
                val userName = userRepo.getUserById(eval.id_user, token)?.name
                    ?: getString(R.string.unknown_user)
                eval.copy(id_user = userName)
            }

            rvEvaluations.adapter = EvaluationAdapter(evaluationsWithUserNames)
        }
    }

    /**
     * Opens the read-only update history of a task.
     */
    private fun openTaskUpdates(taskId: String) {
        val intent = Intent(this, ManagerTaskUpdatesReadonlyActivity::class.java)
        intent.putExtra("TASK_ID", taskId)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
