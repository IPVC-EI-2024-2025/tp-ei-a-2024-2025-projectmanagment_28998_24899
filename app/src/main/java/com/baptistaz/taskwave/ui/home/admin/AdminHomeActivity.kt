package com.baptistaz.taskwave.ui.home.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.home.admin.assignmanager.AssignManagerActivity
import com.baptistaz.taskwave.ui.home.admin.exportstatistics.ExportStatisticsActivity
import com.baptistaz.taskwave.ui.home.admin.manageprojects.overview.ManageProjectsActivity
import com.baptistaz.taskwave.ui.home.admin.manageusers.ManageUsersActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager

/**
 * Admin home screen displaying quick statistics and navigation to key functionalities.
 */
class AdminHomeActivity : BaseLocalizedActivity() {

    private lateinit var viewModel: AdminDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val textProjects = findViewById<TextView>(R.id.text_projects)
        val textUsers = findViewById<TextView>(R.id.text_users)
        val textTasks = findViewById<TextView>(R.id.text_tasks)

        val token = SessionManager.getAccessToken(this) ?: ""

        // Initialize ViewModel with token
        viewModel = ViewModelProvider(this, AdminDashboardViewModelFactory(token))[AdminDashboardViewModel::class.java]

        // Observe and update stats on screen
        viewModel.projectCount.observe(this) { textProjects.text = it.toString() }
        viewModel.userCount.observe(this) { textUsers.text = it.toString() }
        viewModel.taskCount.observe(this) { textTasks.text = it.toString() }

        // Load statistics
        viewModel.loadDashboardData(token)

        // Project management button
        findViewById<LinearLayout>(R.id.button_manage_projects).setOnClickListener {
            startActivity(Intent(this, ManageProjectsActivity::class.java))
        }

        // User management button
        findViewById<LinearLayout>(R.id.button_manage_users).setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        // Assign manager to projects
        findViewById<LinearLayout>(R.id.button_assign_manager).setOnClickListener {
            startActivity(Intent(this, AssignManagerActivity::class.java))
        }

        // Export statistics
        findViewById<LinearLayout>(R.id.button_export_statistics).setOnClickListener {
            startActivity(Intent(this, ExportStatisticsActivity::class.java))
        }

        // View own profile
        findViewById<Button>(R.id.button_view_profile).apply {
            text = getString(R.string.btn_view_profile)
            setOnClickListener {
                val intent = Intent(this@AdminHomeActivity, AdminProfileActivity::class.java)
                SessionManager.getAuthId(this@AdminHomeActivity)?.let { authId ->
                    intent.putExtra("AUTH_ID", authId)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val token = SessionManager.getAccessToken(this) ?: return
        viewModel.loadDashboardData(token) // Refresh data on resume
    }
}
