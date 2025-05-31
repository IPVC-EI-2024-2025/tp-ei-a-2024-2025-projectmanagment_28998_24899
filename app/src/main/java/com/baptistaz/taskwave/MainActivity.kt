package com.baptistaz.taskwave

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.ui.home.HomeActivity
import com.baptistaz.taskwave.utils.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = SessionManager.getAccessToken(this)
        if (!token.isNullOrEmpty()) {
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}