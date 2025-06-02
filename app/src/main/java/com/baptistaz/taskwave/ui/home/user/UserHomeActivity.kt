package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.SessionManager

class UserHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        val logoutButton = findViewById<Button>(R.id.button_logout)
        logoutButton.setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}