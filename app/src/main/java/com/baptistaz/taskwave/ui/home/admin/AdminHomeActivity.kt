package com.baptistaz.taskwave.ui.home.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.home.admin.assignmanager.AssignManagerActivity
import com.baptistaz.taskwave.ui.home.admin.exportstatistics.ExportStatisticsActivity
import com.baptistaz.taskwave.ui.home.admin.manageprojects.ManageProjectsActivity
import com.baptistaz.taskwave.ui.home.admin.manageusers.ManageUsersActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager

class AdminHomeActivity : BaseLocalizedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // Estatísticas simuladas (podem ser dinâmicas mais tarde)
        val numProjects = 12
        val numUsers = 8
        val numTasks = 24

        findViewById<TextView>(R.id.text_projects).text = numProjects.toString()
        findViewById<TextView>(R.id.text_users).text = numUsers.toString()
        findViewById<TextView>(R.id.text_tasks).text = numTasks.toString()

        // Botões personalizados
        findViewById<LinearLayout>(R.id.button_manage_projects).setOnClickListener {
            startActivity(Intent(this, ManageProjectsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_manage_users).setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_assign_manager).setOnClickListener {
            startActivity(Intent(this, AssignManagerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_export_statistics).setOnClickListener {
            startActivity(Intent(this, ExportStatisticsActivity::class.java))
        }

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
}
