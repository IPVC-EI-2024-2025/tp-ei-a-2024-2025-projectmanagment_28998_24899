package com.baptistaz.taskwave.ui.home.admin.manageusers

import User
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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
    private lateinit var inputSearch: EditText
    private lateinit var spinnerFilter: Spinner

    private lateinit var cardTotal: View
    private lateinit var cardAdmins: View
    private lateinit var cardManagers: View
    private lateinit var cardUsers: View

    private lateinit var statTotal: TextView
    private lateinit var statAdmins: TextView
    private lateinit var statManagers: TextView
    private lateinit var statUsers: TextView

    private val userRepository = UserRepository()
    private val allUsers = mutableListOf<User>()
    private val filteredUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Users"

        // Views
        recyclerView = findViewById(R.id.recycler_users)
        btnNewUser = findViewById(R.id.btn_new_user)
        inputSearch = findViewById(R.id.input_search)
        spinnerFilter = findViewById(R.id.spinner_filter)

        // Cards
        cardTotal = findViewById(R.id.card_total)
        cardAdmins = findViewById(R.id.card_admins)
        cardManagers = findViewById(R.id.card_managers)
        cardUsers = findViewById(R.id.card_users)

        statTotal = cardTotal.findViewById(R.id.text_stat_value)
        statAdmins = cardAdmins.findViewById(R.id.text_stat_value)
        statManagers = cardManagers.findViewById(R.id.text_stat_value)
        statUsers = cardUsers.findViewById(R.id.text_stat_value)

        cardTotal.findViewById<TextView>(R.id.text_stat_label).text = "Total"
        cardAdmins.findViewById<TextView>(R.id.text_stat_label).text = "Admins"
        cardManagers.findViewById<TextView>(R.id.text_stat_label).text = "Managers"
        cardUsers.findViewById<TextView>(R.id.text_stat_label).text = "Users"

        adapter = UserAdapter(filteredUsers, { userId -> deleteUser(userId) }) { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("userId", user.id_user)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Filtro
        val roles = listOf("All", "Admin", "Manager", "User")
        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnNewUser.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@ManageUsersActivity) ?: return@launch
            val result = userRepository.getAllUsers(token)
            if (result != null) {
                allUsers.clear()
                allUsers.addAll(result.sortedWith(compareByDescending<User> { it.profileType.equals("ADMIN", true) }.thenBy { it.name }))
                updateStats()
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val query = inputSearch.text.toString().trim().lowercase()
        val selectedFilter = spinnerFilter.selectedItem.toString().lowercase()

        val filtered = allUsers.filter { user ->
            val matchesSearch = user.name.lowercase().contains(query) || user.email.lowercase().contains(query)
            val matchesFilter = when (selectedFilter) {
                "admin" -> user.profileType.equals("ADMIN", true)
                "manager" -> user.profileType.equals("MANAGER", true)
                "user" -> user.profileType.equals("USER", true)
                else -> true
            }
            matchesSearch && matchesFilter
        }

        filteredUsers.clear()
        filteredUsers.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun updateStats() {
        statTotal.text = allUsers.size.toString()
        statAdmins.text = allUsers.count { it.profileType.equals("ADMIN", true) }.toString()
        statManagers.text = allUsers.count { it.profileType.equals("MANAGER", true) }.toString()
        statUsers.text = allUsers.count { it.profileType.equals("USER", true) }.toString()
    }

    fun deleteUser(userId: String) {
        val token = SessionManager.getAccessToken(this) ?: ""
        CoroutineScope(Dispatchers.Main).launch {
            val success = userRepository.deleteUser(userId, token)
            if (success) {
                Toast.makeText(this@ManageUsersActivity, "Utilizador eliminado!", Toast.LENGTH_SHORT).show()
                fetchUsers()
            } else {
                Toast.makeText(this@ManageUsersActivity, "Erro ao eliminar utilizador!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUsers()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
