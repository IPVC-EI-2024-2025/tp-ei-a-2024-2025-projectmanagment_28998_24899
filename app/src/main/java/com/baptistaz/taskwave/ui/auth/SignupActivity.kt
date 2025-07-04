package com.baptistaz.taskwave.ui.auth

import User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
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

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(RetrofitInstance.auth))
        )[AuthViewModel::class.java]

        authViewModel.clearAuthResponse()

        editEmail = findViewById(R.id.edit_email)
        editName = findViewById(R.id.edit_name)
        editMobile = findViewById(R.id.edit_mobile)
        editPassword = findViewById(R.id.edit_password)
        editConfirmPassword = findViewById(R.id.edit_confirm_password)
        val buttonSignup = findViewById<Button>(R.id.registerButton)
        val textLogin = findViewById<TextView>(R.id.signInLink)

        buttonSignup.setOnClickListener {
            val email = editEmail.text.toString()
            val name = editName.text.toString()
            val mobile = editMobile.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (email.isNotBlank() && name.isNotBlank() && mobile.isNotBlank() &&
                password.isNotBlank() && password == confirmPassword
            ) {
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

        textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        authViewModel.authResponse.observe(this) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token
                    val authId = response.body()?.user?.id
                    if (!token.isNullOrEmpty() && !authId.isNullOrEmpty()) {
                        SessionManager.saveAccessToken(this, token)
                        SessionManager.saveAuthId(this, authId)

                        lifecycleScope.launch {
                            val user = User(
                                name = editName.text.toString(),
                                username = editEmail.text.toString().split("@")[0],
                                email = editEmail.text.toString(),
                                password = "", // não guardar a senha localmente
                                profileType = "USER",
                                photo = "",
                                phoneNumber = editMobile.text.toString(),
                                authId = authId
                            )
                            Log.d("SIGNUP_DEBUG", "Tentando criar utilizador: $user")
                            val userRepository = UserRepository()
                            val success = userRepository.createUser(user, token)
                            if (success) {
                                Log.d("SIGNUP_DEBUG", "Perfil criado com sucesso")
                                Toast.makeText(
                                    this@SignupActivity,
                                    getString(R.string.toast_registration_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                                finish()
                            } else {
                                Log.e("SIGNUP_ERROR", "Erro ao criar perfil, mas utilizador registado no Auth")
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
                        Log.e("SIGNUP_ERROR", "Token ou authId nulos: token=$token, authId=$authId")
                        Toast.makeText(
                            this,
                            getString(R.string.toast_invalid_token),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e("SIGNUP_ERROR", "Erro no registo: ${response.errorBody()?.string()}")
                    Toast.makeText(this, getString(R.string.toast_signup_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
