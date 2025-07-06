package com.baptistaz.taskwave.ui.home.manager.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageusers.EditUserActivity
import com.baptistaz.taskwave.ui.home.manager.base.BaseManagerBottomNavActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager settings screen allowing the user to edit their profile or change language.
 */
class ManagerSettingsActivity : BaseManagerBottomNavActivity() {

    override fun getSelectedMenuId(): Int = R.id.nav_settings
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_settings)

        // UI references
        val progress = findViewById<ProgressBar>(R.id.progress_loading)
        val contentLayout = findViewById<LinearLayout>(R.id.layout_manager_settings)
        val txtName = findViewById<TextView>(R.id.text_name)

        // Basic null check to avoid crashes
        if (progress == null || contentLayout == null || txtName == null) {
            Toast.makeText(this, "Error loading components", Toast.LENGTH_SHORT).show()
            return
        }

        loadCurrentManager()

        // Edit profile action
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

        // Change language action
        findViewById<LinearLayout>(R.id.option_change_language).setOnClickListener {
            showLanguageDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentManager()
    }

    /**
     * Loads the current manager's profile from the backend.
     */
    private fun loadCurrentManager() {
        val progress = findViewById<ProgressBar>(R.id.progress_loading)
        val contentLayout = findViewById<LinearLayout>(R.id.layout_manager_settings)
        val txtName = findViewById<TextView>(R.id.text_name)
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        // Show progress while loading user info
        progress.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        // Load user data
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
