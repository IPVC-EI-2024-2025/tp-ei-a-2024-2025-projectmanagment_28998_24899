package com.baptistaz.taskwave

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.auth.view.LoginActivity
import com.baptistaz.taskwave.ui.home.admin.AdminHomeActivity
import com.baptistaz.taskwave.ui.home.manager.project.list.ManagerProjectsAreaActivity
import com.baptistaz.taskwave.ui.home.user.home.UserHomeActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Entry point of the app.
 * Redirects the user to the appropriate home screen based on their profile type.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = SessionManager.getAccessToken(this)
        val authId = SessionManager.getAuthId(this)

        // If no session exists, redirect to login
        if (token.isNullOrEmpty() || authId.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Fetch user profile and redirect accordingly
        lifecycleScope.launch {
            val user = UserRepository().getUserByAuthId(authId, token)

            val target = when (user?.profileType?.uppercase()) {
                "ADMIN"  -> AdminHomeActivity::class.java
                "GESTOR" -> ManagerProjectsAreaActivity::class.java
                "USER"   -> UserHomeActivity::class.java
                else     -> LoginActivity::class.java
            }

            startActivity(Intent(this@MainActivity, target))
            finish()
        }
    }
}
