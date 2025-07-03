package com.baptistaz.taskwave

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.ui.home.admin.AdminHomeActivity
import com.baptistaz.taskwave.ui.home.manager.ManagerProjectsAreaActivity
import com.baptistaz.taskwave.ui.home.user.UserHomeActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = SessionManager.getAccessToken(this)
        val authId = SessionManager.getAuthId(this)

        if (token.isNullOrEmpty() || authId.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            val user = UserRepository().getUserByAuthId(authId, token)

            val target = when (user?.profileType?.uppercase()) {
                "ADMIN" -> AdminHomeActivity::class.java
                "GESTOR" -> ManagerProjectsAreaActivity::class.java
                "USER" -> UserHomeActivity::class.java
                else -> LoginActivity::class.java
            }

            startActivity(Intent(this@MainActivity, target))
            finish()
        }
    }
}
