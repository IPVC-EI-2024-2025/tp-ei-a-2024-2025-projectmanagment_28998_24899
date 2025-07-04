package com.baptistaz.taskwave.ui.home.manager

import BaseManagerBottomNavActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageusers.EditUserActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerSettingsActivity : BaseManagerBottomNavActivity() {

    override fun getSelectedMenuId(): Int = R.id.nav_settings
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_settings)

        loadCurrentManager()

        findViewById<LinearLayout>(R.id.option_edit_profile).setOnClickListener {
            currentUserId?.let { id ->
                Intent(this, EditUserActivity::class.java)
                    .putExtra("USER_ID", id)
                    .also { startActivity(it) }
            } ?: Toast.makeText(
                this,
                getString(R.string.toast_loading_profile),
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<LinearLayout>(R.id.option_change_language).setOnClickListener {
            // O Spinner já trata da mudança
        }

        findViewById<Switch>(R.id.switch_notifications).setOnCheckedChangeListener { _, isChecked ->
            // Guardar preferência se necessário
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentManager()
    }

    private fun loadCurrentManager() {
        val progress = findViewById<ProgressBar>(R.id.progress_loading)
        val contentLayout = findViewById<LinearLayout>(R.id.layout_user_settings)
        val txtName = findViewById<TextView>(R.id.text_name)
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        progress.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            val user = UserRepository().getUserByAuthId(authId, token)
            progress.visibility = View.GONE
            if (user != null) {
                contentLayout.visibility = View.VISIBLE
                txtName.text = user.name ?: ""
                currentUserId = user.id_user
            } else {
                Toast.makeText(
                    this@ManagerSettingsActivity,
                    getString(R.string.error_loading_profile),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
