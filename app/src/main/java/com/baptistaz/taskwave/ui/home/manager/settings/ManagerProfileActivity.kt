package com.baptistaz.taskwave.ui.home.manager.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.auth.view.LoginActivity
import com.baptistaz.taskwave.ui.home.manager.base.BaseManagerBottomNavActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manager profile screen that shows the logged-in user's details and allows logout.
 */
class ManagerProfileActivity : BaseManagerBottomNavActivity() {
    override fun getSelectedMenuId() = R.id.nav_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_profile)

        // UI elements
        val imgProfile = findViewById<ImageView>(R.id.image_profile)
        val txtName = findViewById<TextView>(R.id.text_name)
        val txtUsername = findViewById<TextView>(R.id.text_username)
        val txtUsernameCard = findViewById<TextView>(R.id.text_username_card)
        val txtEmail = findViewById<TextView>(R.id.text_email)
        val txtPhone = findViewById<TextView>(R.id.text_phone)
        val btnLogout = findViewById<LinearLayout>(R.id.button_logout)

        // Clear all fields initially
        txtName.text = ""
        txtUsername.text = ""
        txtUsernameCard.text = ""
        txtEmail.text = ""
        txtPhone.text = ""

        // Retrieve token and authId from session
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        // Fetch user data from repository
        CoroutineScope(Dispatchers.Main).launch {
            val user = UserRepository().getUserByAuthId(authId, token)
            user?.let {
                txtName.text = it.name
                txtUsername.text = it.username
                txtUsernameCard.text = it.username
                txtEmail.text = it.email
                txtPhone.text = it.phoneNumber
            }
        }

        // Handle logout button click
        btnLogout.setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
