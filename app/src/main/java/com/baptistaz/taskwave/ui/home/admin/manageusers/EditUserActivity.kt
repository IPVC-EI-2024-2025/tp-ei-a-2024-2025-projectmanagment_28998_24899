package com.baptistaz.taskwave.ui.home.admin.manageusers

import UserUpdate
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

class EditUserActivity : AppCompatActivity() {

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var usernameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var saveBtn: Button

    private var userId: String? = null
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit User"

        // UI refs
        nameEdit = findViewById(R.id.edit_name)
        emailEdit = findViewById(R.id.edit_email)
        usernameEdit = findViewById(R.id.edit_username)
        phoneEdit = findViewById(R.id.edit_phonenumber)
        spinnerRole = findViewById(R.id.spinner_role)
        saveBtn = findViewById(R.id.btn_save)

        // Preenche spinner com opções de role
        val roles = arrayOf("Gestor", "User")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // Recebe o userId via intent
        userId = intent.getStringExtra("userId")
        Log.d("EDIT_USER", "userId recebido no intent: $userId")

        // Busca dados do user
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""
            Log.d("EDIT_USER", "Access token: $token")
            userId?.let { id ->
                Log.d("EDIT_USER", "A buscar user do repo para o id: $id")
                val user = userRepository.getUserById(id, token)
                Log.d("EDIT_USER", "user recebido do repo: $user")
                user?.let { userData ->
                    nameEdit.setText(userData.name)
                    emailEdit.setText(userData.email)
                    usernameEdit.setText(userData.username)
                    phoneEdit.setText(userData.phoneNumber ?: "")
                    spinnerRole.setSelection(roles.indexOfFirst { it.equals(userData.profileType, true) })
                } ?: run {
                    Log.e("EDIT_USER", "User não encontrado no repo para o id: $id")
                }
            } ?: run {
                Log.e("EDIT_USER", "userId veio nulo no intent!")
            }
        }

        // Guardar alterações
        saveBtn.setOnClickListener {
            val token = SessionManager.getAccessToken(this@EditUserActivity) ?: ""
            val updatedUser = UserUpdate(
                name = nameEdit.text.toString(),
                email = emailEdit.text.toString(),
                username = usernameEdit.text.toString(),
                phonenumber = phoneEdit.text.toString(),
                profiletype = ProfileType.fromLabel(spinnerRole.selectedItem.toString()).db
            )
            Log.d("EDIT_USER", "Vai enviar update PATCH: $updatedUser para id: $userId")
            CoroutineScope(Dispatchers.Main).launch {
                val success = userRepository.updateUser(userId ?: "", updatedUser, token)
                if (success) {
                    Toast.makeText(this@EditUserActivity, "User updated!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditUserActivity, "Error updating user!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
