package com.baptistaz.taskwave.ui.auth.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.auth.viewmodel.AuthViewModel
import com.baptistaz.taskwave.ui.auth.viewmodel.AuthViewModelFactory
import com.baptistaz.taskwave.ui.home.admin.AdminHomeActivity
import com.baptistaz.taskwave.ui.home.manager.project.list.ManagerProjectsAreaActivity
import com.baptistaz.taskwave.ui.home.user.home.UserHomeActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : BaseLocalizedActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize AuthViewModel with factory
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(RetrofitInstance.auth))
        ).get(AuthViewModel::class.java)

        // Clear previous authentication responses
        authViewModel.clearAuthResponse()

        // Bind UI elements
        val editEmail = findViewById<TextInputEditText>(R.id.edit_email)
        val editPassword = findViewById<TextInputEditText>(R.id.edit_password)
        val buttonLogin = findViewById<Button>(R.id.signInButton)
        val textSignup = findViewById<TextView>(R.id.signUpLink)
        val textSignupPrompt = findViewById<TextView>(R.id.signUpPrompt)
        val textForgotPassword = findViewById<TextView>(R.id.forgotPasswordText)

        // Apply translated UI strings
        editEmail.hint = getString(R.string.email_hint)
        editPassword.hint = getString(R.string.password_hint)
        buttonLogin.text = getString(R.string.sign_in)
        textSignup.text = getString(R.string.signup_link)
        textForgotPassword.text = getString(R.string.forgot_password)
        textSignupPrompt.text = getString(R.string.signup_prompt)

        // Login button click
        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(this, getString(R.string.login_prompt), Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to signup screen
        textSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Show toast for forgotten password (placeholder for future recovery)
        textForgotPassword.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.forgot_password),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe login response
        authViewModel.authResponse.observe(this) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    val authId = response.body()?.user?.id

                    if (!token.isNullOrEmpty() && !authId.isNullOrEmpty()) {
                        Log.d("LOGIN_DEBUG", "Successful login - token: $token, authId: $authId")

                        // Store token and auth ID locally
                        SessionManager.saveAccessToken(this, token)
                        SessionManager.saveAuthId(this, authId)

                        // Load full user profile by auth ID
                        lifecycleScope.launch {
                            val userRepository = UserRepository()
                            val user = userRepository.getUserByAuthId(authId, token)

                            if (user != null) {
                                // Store user ID locally
                                SessionManager.saveUserId(this@LoginActivity, user.id_user ?: "")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Profile: ${user.profileType}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Redirect based on user profile
                                when (user.profileType.uppercase()) {
                                    "ADMIN" -> startActivity(Intent(this@LoginActivity, AdminHomeActivity::class.java))
                                    "GESTOR" -> startActivity(Intent(this@LoginActivity, ManagerProjectsAreaActivity::class.java))
                                    "USER" -> startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                                    else -> {
                                        Log.e("LOGIN_ERROR", "Invalid profile type: ${user.profileType}")
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Invalid profile!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        SessionManager.clearAccessToken(this@LoginActivity)
                                    }
                                }

                                finish()
                            } else {
                                Log.e("LOGIN_ERROR", "Failed to load user for authId: $authId")
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Error loading profile. You may need to recreate it.",
                                    Toast.LENGTH_LONG
                                ).show()
                                SessionManager.clearAccessToken(this@LoginActivity)
                            }
                        }
                    } else {
                        Log.e("LOGIN_ERROR", "Missing token or authId: token=$token, authId=$authId")
                        Toast.makeText(
                            this,
                            "Login error: invalid token or auth ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("LOGIN_ERROR", "Login failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
