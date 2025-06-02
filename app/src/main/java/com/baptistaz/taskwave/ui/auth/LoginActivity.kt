package com.baptistaz.taskwave.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.ui.home.admin.AdminHomeActivity
import com.baptistaz.taskwave.ui.home.manager.ManagerHomeActivity
import com.baptistaz.taskwave.ui.home.user.UserHomeActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authViewModel = ViewModelProvider(this, AuthViewModelFactory(AuthRepository(RetrofitInstance.auth)))
            .get(AuthViewModel::class.java)

        authViewModel.clearAuthResponse()

        val editEmail = findViewById<TextInputEditText>(R.id.edit_email)
        val editPassword = findViewById<TextInputEditText>(R.id.edit_password)
        val buttonLogin = findViewById<Button>(R.id.signInButton)
        val textSignup = findViewById<TextView>(R.id.signUpLink)
        val textForgotPassword = findViewById<TextView>(R.id.forgotPasswordText)

        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                authViewModel.login(email, password)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        textSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de recuperar palavra-passe em breve!", Toast.LENGTH_SHORT).show()
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
                            val userRepository = UserRepository()
                            val user = userRepository.getUserByAuthId(authId, token)

                            if (user != null) {
                                Log.d("DEBUG", "authId: $authId")
                                Log.d("DEBUG", "user: $user")
                                Toast.makeText(this@LoginActivity, "Perfil: ${user.profileType}", Toast.LENGTH_SHORT).show()

                                when (user.profileType.uppercase()) {
                                    "ADMIN" -> startActivity(Intent(this@LoginActivity, AdminHomeActivity::class.java))
                                    "GESTOR" -> startActivity(Intent(this@LoginActivity, ManagerHomeActivity::class.java))
                                    "USER" -> startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                                    else -> {
                                        Toast.makeText(this@LoginActivity, "Perfil inválido!", Toast.LENGTH_SHORT).show()
                                        SessionManager.clearAccessToken(this@LoginActivity)
                                    }
                                }
                                finish()
                            } else {
                                Log.e("API_ERROR", "Erro ao obter utilizador: resposta nula ou falha na API")
                                Toast.makeText(this@LoginActivity, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
                                SessionManager.clearAccessToken(this@LoginActivity)
                            }
                        }
                    }
                } else {
                    Log.e("API_ERROR", response.errorBody()?.string() ?: "Erro desconhecido")
                    Toast.makeText(this, "Erro no login", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}