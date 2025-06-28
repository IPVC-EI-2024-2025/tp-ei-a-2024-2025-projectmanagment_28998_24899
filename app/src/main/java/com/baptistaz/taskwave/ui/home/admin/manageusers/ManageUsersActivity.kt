package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var btnNewUser: Button

    private val userRepository = UserRepository()
    private val users = mutableListOf<User>() // Ou usa ViewModel + LiveData se quiseres

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Users"

        recyclerView = findViewById(R.id.recycler_users)
        btnNewUser = findViewById(R.id.btn_new_user) // Referência ao botão "+ New User"

        adapter = UserAdapter(users) { userId ->
            deleteUser(userId)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnNewUser.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

        fetchUsers()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        fetchUsers()
    }

    private fun fetchUsers() {
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@ManageUsersActivity) ?: ""
            val userList = userRepository.getAllUsers(token)
            if (userList != null) {
                // Ordena a lista: admins primeiro, depois os outros por nome
                val sortedList = userList.sortedWith(compareByDescending<User> { it.profileType.equals("ADMIN", ignoreCase = true) }
                    .thenBy { it.name.lowercase() })
                users.clear()
                users.addAll(sortedList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun deleteUser(userId: String) {
        val token = SessionManager.getAccessToken(this) ?: ""
        CoroutineScope(Dispatchers.Main).launch {
            val success = userRepository.deleteUser(userId, token)
            if (success) {
                Toast.makeText(this@ManageUsersActivity, "Utilizador eliminado!", Toast.LENGTH_SHORT).show()
                fetchUsers() // refresca a lista!
            } else {
                Toast.makeText(this@ManageUsersActivity, "Erro ao eliminar utilizador!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
