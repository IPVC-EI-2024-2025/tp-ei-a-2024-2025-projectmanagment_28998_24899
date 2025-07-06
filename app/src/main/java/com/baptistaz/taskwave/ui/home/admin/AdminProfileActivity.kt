package com.baptistaz.taskwave.ui.home.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class AdminProfileActivity : BaseLocalizedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val textName  = findViewById<TextView>(R.id.text_admin_name)
        val textEmail = findViewById<TextView>(R.id.text_admin_email)
        val btnLogout = findViewById<Button>(R.id.button_logout)

        // Texto inicial (placeholders)
        textName.text  = getString(R.string.loading)
        textEmail.text = getString(R.string.loading_email)

        // Carrega dados do Admin
        val authId = intent.getStringExtra("AUTH_ID") ?: SessionManager.getAuthId(this)
        if (authId != null) {
            lifecycleScope.launch {
                val token = SessionManager.getAccessToken(this@AdminProfileActivity) ?: return@launch
                UserRepository().getUserByAuthId(authId, token)?.let { admin ->
                    textName.text  = admin.name
                    textEmail.text = admin.email
                }
            }
        }

        // Logout
        btnLogout.text = getString(R.string.btn_logout)
        btnLogout.setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
