package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.UserUpdate
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditUserActivity : AppCompatActivity() {

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var profileText: TextView
    private lateinit var saveBtn: Button

    private var userId: String = ""
    private var loadedUser: User? = null
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        // Toolbar com botão back
        findViewById<Toolbar>(R.id.toolbar).apply {
            title = "Edit User"
            setNavigationIcon(R.drawable.ic_arrow_right)
            setNavigationOnClickListener { finish() }
        }

        // Referências UI
        nameEdit     = findViewById(R.id.edit_name)
        emailEdit    = findViewById(R.id.edit_email)
        usernameEdit = findViewById(R.id.edit_username)
        phoneEdit    = findViewById(R.id.edit_phonenumber)
        profileText  = findViewById(R.id.text_profile_type)
        saveBtn      = findViewById(R.id.btn_save)

        // Recebe o USER_ID
        userId = intent.getStringExtra("USER_ID")
            ?: run {
                Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        // Carrega dados do utilizador
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""
            val user = userRepository.getUserById(userId, token)
            if (user != null) {
                loadedUser = user
                nameEdit.setText(user.name)
                emailEdit.setText(user.email)
                usernameEdit.setText(user.username)
                phoneEdit.setText(user.phoneNumber ?: "")
                profileText.text = user.profileType ?: "User"
            } else {
                Toast.makeText(this@EditUserActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Botão Guardar
        saveBtn.setOnClickListener {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""

            val updated = UserUpdate(
                name        = nameEdit.text.toString(),
                email       = emailEdit.text.toString(),
                username    = usernameEdit.text.toString(),
                phonenumber = phoneEdit.text.toString(),
                profiletype = loadedUser?.profileType ?: "User"
            )

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    userRepository.updateUser(userId, updated, token)
                    Toast.makeText(this@EditUserActivity, "User updated!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("EDIT_USER", "Error updating user", e)
                    Toast.makeText(this@EditUserActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
