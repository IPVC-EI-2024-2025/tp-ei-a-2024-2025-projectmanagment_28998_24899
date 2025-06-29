package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.utils.ProfileType
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateUserActivity : AppCompatActivity() {

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var createBtn: Button

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create User"

        // UI refs
        nameEdit = findViewById(R.id.edit_name)
        emailEdit = findViewById(R.id.edit_email)
        usernameEdit = findViewById(R.id.edit_username)
        passwordEdit = findViewById(R.id.edit_password)
        phoneEdit = findViewById(R.id.edit_phonenumber)
        spinnerRole = findViewById(R.id.spinner_role)
        createBtn = findViewById(R.id.btn_create)

        // Preenche spinner sem opção Admin
        val roles = arrayOf("Gestor", "User")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        createBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString()
            val phone = phoneEdit.text.toString().trim()
            val profileType = ProfileType.fromLabel(spinnerRole.selectedItem.toString()).db

            // Validação simples
            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preenche todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                id_user = null,
                name = name,
                email = email,
                username = username,
                password = password,
                phoneNumber = phone,
                profileType = profileType,
                photo = "",
                authId = null
            )

            CoroutineScope(Dispatchers.Main).launch {
                val token = SessionManager.getAccessToken(this@CreateUserActivity) ?: ""
                Log.d("CREATE_USER", "Vai criar user: $newUser")
                val success = userRepository.createUser(newUser, token)
                if (success) {
                    Toast.makeText(this@CreateUserActivity, "User created!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateUserActivity, "Error creating user!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
