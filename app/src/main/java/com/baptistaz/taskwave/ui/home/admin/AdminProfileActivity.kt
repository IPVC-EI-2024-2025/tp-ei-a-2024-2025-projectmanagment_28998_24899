package com.baptistaz.taskwave.ui.home.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class AdminProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        // Carrega dados do Admin
        val authId = intent.getStringExtra("AUTH_ID") ?: SessionManager.getAuthId(this)
        if (authId != null) {
            lifecycleScope.launch {
                val token = SessionManager.getAccessToken(this@AdminProfileActivity) ?: return@launch
                UserRepository().getUserByAuthId(authId, token)?.let { admin ->
                    findViewById<TextView>(R.id.text_admin_name).text = admin.name
                    findViewById<TextView>(R.id.text_admin_email).text = admin.email
                    // preencha outros campos…
                }
            }
        }

        // Logout agora aqui
        findViewById<Button>(R.id.button_logout).setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
