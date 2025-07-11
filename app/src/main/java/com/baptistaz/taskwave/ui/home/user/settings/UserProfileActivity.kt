package com.baptistaz.taskwave.ui.home.user.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.auth.view.LoginActivity
import com.baptistaz.taskwave.ui.home.user.base.BaseBottomNavActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays the user's profile info
 * Allows logout and is part of the bottom navigation.
 */
class UserProfileActivity : BaseBottomNavActivity() {
    override fun getSelectedMenuId() = R.id.nav_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize UI components
        val imgProfile       = findViewById<ImageView>(R.id.image_profile)
        val txtName          = findViewById<TextView>(R.id.text_name)
        val txtUsername      = findViewById<TextView>(R.id.text_username)
        val txtUsernameCard  = findViewById<TextView>(R.id.text_username_card)
        val txtEmail         = findViewById<TextView>(R.id.text_email)
        val txtPhone         = findViewById<TextView>(R.id.text_phone)
        val btnLogout        = findViewById<LinearLayout>(R.id.button_logout)

        // Clear fields while loading
        txtName.text = ""
        txtUsername.text = ""
        txtUsernameCard.text = ""
        txtEmail.text = ""
        txtPhone.text = ""

        // Get session data
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        // Load user data from backend
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

        // Logout action
        btnLogout.setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
