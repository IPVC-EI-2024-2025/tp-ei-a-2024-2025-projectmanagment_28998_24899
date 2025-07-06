package com.baptistaz.taskwave.ui.home.admin.manageusers

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Admin screen for managing users.
 */
class ManageUsersActivity : BaseLocalizedActivity() {

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var btnNewUser: Button
    private lateinit var inputSearch: EditText
    private lateinit var spinnerFilter: Spinner

    // Statistic cards
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

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_users)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_manage_users)

        // Bind views
        recyclerView = findViewById(R.id.recycler_users)
        btnNewUser = findViewById(R.id.btn_new_user)
        inputSearch = findViewById(R.id.input_search)
        spinnerFilter = findViewById(R.id.spinner_filter)

        // Statistic cards setup
        cardTotal = findViewById(R.id.card_total)
        cardAdmins = findViewById(R.id.card_admins)
        cardManagers = findViewById(R.id.card_managers)
        cardUsers = findViewById(R.id.card_users)

        cardTotal.findViewById<TextView>(R.id.text_stat_label).text = getString(R.string.stat_total)
        cardAdmins.findViewById<TextView>(R.id.text_stat_label).text = getString(R.string.stat_admins)
        cardManagers.findViewById<TextView>(R.id.text_stat_label).text = getString(R.string.stat_managers)
        cardUsers.findViewById<TextView>(R.id.text_stat_label).text = getString(R.string.stat_users)

        statTotal = cardTotal.findViewById(R.id.text_stat_value)
        statAdmins = cardAdmins.findViewById(R.id.text_stat_value)
        statManagers = cardManagers.findViewById(R.id.text_stat_value)
        statUsers = cardUsers.findViewById(R.id.text_stat_value)

        // Recycler setup
        adapter = UserAdapter(filteredUsers, { userId -> deleteUser(userId) }) { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("userId", user.id_user)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Role filter dropdown
        val roles = listOf(
            getString(R.string.stat_total),
            getString(R.string.stat_admins),
            getString(R.string.stat_managers),
            getString(R.string.stat_users)
        )
        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Search listener
        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilters()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Add new user
        btnNewUser.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

        fetchUsers()
    }

    /**
     * Fetches all users from the backend and updates UI
     */
    private fun fetchUsers() {
        CoroutineScope(Dispatchers.Main).launch {
            val token = SessionManager.getAccessToken(this@ManageUsersActivity) ?: return@launch
            val result = userRepository.getAllUsers(token)
            if (result != null) {
                allUsers.clear()
                allUsers.addAll(
                    result.sortedWith(
                        compareBy<User> {
                            when (it.profileType.uppercase()) {
                                "ADMIN" -> 0
                                "GESTOR" -> 1
                                else -> 2
                            }
                        }.thenBy { it.name }
                    )
                )
                updateStats()
                applyFilters()
            }
        }
    }

    /**
     * Applies search and role filters to the user list
     */
    private fun applyFilters() {
        val query = inputSearch.text.toString().trim().lowercase()
        val selectedFilter = spinnerFilter.selectedItem.toString()

        val filtered = allUsers.filter { user ->
            val matchesSearch = user.name.lowercase().contains(query) || user.email.lowercase().contains(query)
            val matchesFilter = when (selectedFilter) {
                getString(R.string.stat_admins) -> user.profileType.equals("ADMIN", true)
                getString(R.string.stat_managers) -> user.profileType.equals("GESTOR", true)
                getString(R.string.stat_users) -> user.profileType.equals("USER", true)
                else -> true
            }
            matchesSearch && matchesFilter
        }

        filteredUsers.clear()
        filteredUsers.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    /**
     * Updates the counts in each statistics card
     */
    private fun updateStats() {
        statTotal.text = allUsers.size.toString()
        statAdmins.text = allUsers.count { it.profileType.equals("ADMIN", true) }.toString()
        statManagers.text = allUsers.count { it.profileType.equals("GESTOR", true) }.toString()
        statUsers.text = allUsers.count { it.profileType.equals("USER", true) }.toString()
    }

    /**
     * Deletes a user by ID and refreshes the list
     */
    fun deleteUser(userId: String) {
        val token = SessionManager.getAccessToken(this) ?: ""
        CoroutineScope(Dispatchers.Main).launch {
            val success = userRepository.deleteUser(userId, token)
            if (success) {
                Toast.makeText(this@ManageUsersActivity, getString(R.string.delete_user), Toast.LENGTH_SHORT).show()
                fetchUsers()
            } else {
                Toast.makeText(this@ManageUsersActivity, getString(R.string.error_user_update, ""), Toast.LENGTH_SHORT).show()
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
