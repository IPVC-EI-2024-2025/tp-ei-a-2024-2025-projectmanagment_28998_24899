package com.baptistaz.taskwave.ui.home.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.SessionManager

class AdminHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // Estatísticas simuladas
        val numProjects = 12
        val numUsers = 8
        val numTasks = 24

        findViewById<TextView>(R.id.text_projects).text = numProjects.toString()
        findViewById<TextView>(R.id.text_users).text = numUsers.toString()
        findViewById<TextView>(R.id.text_tasks).text = numTasks.toString()

        // Botões personalizados (todos são LinearLayout)
        findViewById<LinearLayout>(R.id.button_manage_projects).setOnClickListener {
            // startActivity(Intent(this, ManageProjectsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_manage_users).setOnClickListener {
            // startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_assign_manager).setOnClickListener {
            // startActivity(Intent(this, AssignManagerActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.button_export_statistics).setOnClickListener {
            // startActivity(Intent(this, ExportStatisticsActivity::class.java))
        }

        // Logout (ainda é Button, por isso mantemos como está)
        findViewById<Button>(R.id.button_logout)?.setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
