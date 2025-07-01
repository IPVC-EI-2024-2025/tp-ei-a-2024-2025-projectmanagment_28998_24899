package com.baptistaz.taskwave.ui.home.admin.manageusers

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
import com.baptistaz.taskwave.data.model.UserUpdate
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.utils.ProfileType
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditUserActivity : AppCompatActivity() {

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var saveBtn: Button

    private var userId: String = ""
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        // Toolbar with back arrow
        findViewById<Toolbar>(R.id.toolbar).apply {
            title = "Edit User"
            setNavigationIcon(R.drawable.ic_arrow_right) // or your back icon
            setNavigationOnClickListener { finish() }
        }

        // UI refs
        nameEdit      = findViewById(R.id.edit_name)
        emailEdit     = findViewById(R.id.edit_email)
        usernameEdit  = findViewById(R.id.edit_username)
        phoneEdit     = findViewById(R.id.edit_phonenumber)
        spinnerRole   = findViewById(R.id.spinner_role)
        saveBtn       = findViewById(R.id.btn_save)

        // Prepare Role spinner
        val roles = listOf("Admin", "Manager", "User")
        spinnerRole.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, roles
        )

        // Extract USER_ID from Intent
        userId = intent.getStringExtra("USER_ID")
            ?: run {
                Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        Log.d("EDIT_USER", "Editing user ID = $userId")

        // Fetch and fill in the user’s current data
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""
            val user  = userRepository.getUserById(userId, token)
            if (user != null) {
                nameEdit.setText(user.name)
                emailEdit.setText(user.email)
                usernameEdit.setText(user.username)
                phoneEdit.setText(user.phoneNumber ?: "")
                // select correct role
                roles.indexOfFirst { it.equals(user.profileType, ignoreCase = true) }
                    .takeIf { it >= 0 }
                    ?.let { spinnerRole.setSelection(it) }
            } else {
                Toast.makeText(this@EditUserActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Save button: build UserUpdate and PATCH
        saveBtn.setOnClickListener {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""
            val updated = UserUpdate(
                name        = nameEdit.text.toString(),
                email       = emailEdit.text.toString(),
                username    = usernameEdit.text.toString(),
                phonenumber = phoneEdit.text.toString(),
                profiletype = ProfileType.fromLabel(spinnerRole.selectedItem as String).db
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
