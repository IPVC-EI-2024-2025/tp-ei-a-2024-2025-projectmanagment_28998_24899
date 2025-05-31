package com.baptistaz.taskwave.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        authViewModel = ViewModelProvider(this, AuthViewModelFactory(AuthRepository(RetrofitInstance.auth)))
            .get(AuthViewModel::class.java)

        authViewModel.clearAuthResponse()

        val editEmail = findViewById<TextInputEditText>(R.id.edit_email)
        val editName = findViewById<TextInputEditText>(R.id.edit_name)
        val editMobile = findViewById<TextInputEditText>(R.id.edit_mobile)
        val editPassword = findViewById<TextInputEditText>(R.id.edit_password)
        val editConfirmPassword = findViewById<TextInputEditText>(R.id.edit_confirm_password)
        val buttonSignup = findViewById<Button>(R.id.registerButton)
        val textLogin = findViewById<TextView>(R.id.signInLink)

        buttonSignup.setOnClickListener {
            val email = editEmail.text.toString()
            val name = editName.text.toString()
            val mobile = editMobile.text.toString()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()
            if (email.isNotBlank() && name.isNotBlank() && mobile.isNotBlank() &&
                password.isNotBlank() && password == confirmPassword) {
                authViewModel.signup(email, password)
            } else {
                Toast.makeText(this, "Verifique os campos", Toast.LENGTH_SHORT).show()
            }
        }

        textLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        authViewModel.authResponse.observe(this) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    Toast.makeText(this, "Registo bem-sucedido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Erro no registo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}