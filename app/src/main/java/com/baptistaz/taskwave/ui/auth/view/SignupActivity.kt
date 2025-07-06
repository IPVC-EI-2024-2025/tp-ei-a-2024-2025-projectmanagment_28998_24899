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
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.auth.viewmodel.AuthViewModel
import com.baptistaz.taskwave.ui.auth.viewmodel.AuthViewModelFactory
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SignupActivity : BaseLocalizedActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var editEmail: TextInputEditText
    private lateinit var editName: TextInputEditText
    private lateinit var editMobile: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize ViewModel with AuthRepository
        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(RetrofitInstance.auth))
        )[AuthViewModel::class.java]

        // Clear any previous authentication state
        authViewModel.clearAuthResponse()

        // Bind form fields
        editEmail = findViewById(R.id.edit_email)
        editName = findViewById(R.id.edit_name)
        editMobile = findViewById(R.id.edit_mobile)
        editPassword = findViewById(R.id.edit_password)
        editConfirmPassword = findViewById(R.id.edit_confirm_password)

        val buttonSignup = findViewById<Button>(R.id.registerButton)
        val textLogin = findViewById<TextView>(R.id.signInLink)

        // Handle signup button click
        buttonSignup.setOnClickListener {
            val email = editEmail.text.toString()
            val name = editName.text.toString()
            val mobile = editMobile.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            // Check field validity
            if (email.isNotBlank() && name.isNotBlank() && mobile.isNotBlank() &&
                password.isNotBlank() && password == confirmPassword
            ) {
                // Trigger signup via ViewModel
                lifecycleScope.launch {
                    authViewModel.signup(email, password)
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.toast_check_fields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Redirect to login screen
        textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Observe signup response from ViewModel
        authViewModel.authResponse.observe(this) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    val authId = response.body()?.user?.id

                    if (!token.isNullOrEmpty() && !authId.isNullOrEmpty()) {
                        // Save token and auth ID locally
                        SessionManager.saveAccessToken(this, token)
                        SessionManager.saveAuthId(this, authId)

                        // Register additional profile data in the backend
                        lifecycleScope.launch {
                            val user = User(
                                name = editName.text.toString(),
                                username = editEmail.text.toString().split("@")[0],
                                email = editEmail.text.toString(),
                                password = "", // Do not store password locally
                                profileType = "USER",
                                photo = "",
                                phoneNumber = editMobile.text.toString(),
                                authId = authId
                            )

                            Log.d("SIGNUP_DEBUG", "Attempting to create user profile: $user")

                            val userRepository = UserRepository()
                            val success = userRepository.createUser(user, token)

                            if (success) {
                                Log.d("SIGNUP_DEBUG", "User profile created successfully")
                                Toast.makeText(
                                    this@SignupActivity,
                                    getString(R.string.toast_registration_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Log.e("SIGNUP_ERROR", "Failed to create profile, but user registered in Auth")
                                Toast.makeText(
                                    this@SignupActivity,
                                    getString(R.string.toast_profile_creation_failed),
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            }
                        }
                    } else {
                        Log.e("SIGNUP_ERROR", "Token or authId is null: token=$token, authId=$authId")
                        Toast.makeText(
                            this,
                            getString(R.string.toast_invalid_token),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle signup failure
                    Log.e("SIGNUP_ERROR", "Signup failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this, getString(R.string.toast_signup_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
