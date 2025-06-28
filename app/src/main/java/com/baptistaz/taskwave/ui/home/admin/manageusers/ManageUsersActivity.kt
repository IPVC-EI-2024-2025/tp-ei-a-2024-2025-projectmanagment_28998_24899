package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.os.Bundle
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
        adapter = UserAdapter(users)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Busca os users numa coroutine
        CoroutineScope(Dispatchers.Main).launch {
            // Vai buscar o access token que está guardado nas SharedPreferences
            val token = SessionManager.getAccessToken(this@ManageUsersActivity) ?: ""
            val userList = userRepository.getAllUsers(token)
            if (userList != null) {
                users.clear()
                users.addAll(userList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}

