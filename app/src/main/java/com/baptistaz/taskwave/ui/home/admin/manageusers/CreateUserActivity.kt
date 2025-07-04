package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.ProfileType
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateUserActivity : BaseLocalizedActivity() {

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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_create_user)

        nameEdit = findViewById(R.id.edit_name)
        emailEdit = findViewById(R.id.edit_email)
        usernameEdit = findViewById(R.id.edit_username)
        passwordEdit = findViewById(R.id.edit_password)
        phoneEdit = findViewById(R.id.edit_phonenumber)
        spinnerRole = findViewById(R.id.spinner_role)
        createBtn = findViewById(R.id.btn_create)

        val roles = arrayOf("Gestor", "User") // Podes internacionalizar se quiseres
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        createBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString()
            val phone = phoneEdit.text.toString().trim()
            val profileType = ProfileType.fromLabel(spinnerRole.selectedItem.toString()).db

            if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fields_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                val token = SessionManager.getAccessToken(this@CreateUserActivity) ?: ""
                val authRepo = AuthRepository(RetrofitInstance.auth)

                try {
                    val authResp = authRepo.signup(email, password)
                    if (authResp.isSuccessful && authResp.body()?.user?.id != null) {
                        val authId = authResp.body()!!.user.id
                        Log.d("CREATE_USER", "User criado no Auth com id: $authId")

                        val newUser = User(
                            id_user = null,
                            name = name,
                            email = email,
                            username = username,
                            password = password,
                            phoneNumber = phone,
                            profileType = profileType,
                            photo = "",
                            authId = authId
                        )

                        val success = userRepository.createUser(newUser, token)
                        if (success) {
                            Toast.makeText(this@CreateUserActivity, getString(R.string.success_user_created), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@CreateUserActivity, getString(R.string.error_user_db), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@CreateUserActivity, getString(R.string.error_user_auth), Toast.LENGTH_SHORT).show()
                        Log.e("CREATE_USER", "Erro Auth: ${authResp.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CreateUserActivity, "${getString(R.string.error_unexpected)}: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("CREATE_USER", "Erro inesperado", e)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
