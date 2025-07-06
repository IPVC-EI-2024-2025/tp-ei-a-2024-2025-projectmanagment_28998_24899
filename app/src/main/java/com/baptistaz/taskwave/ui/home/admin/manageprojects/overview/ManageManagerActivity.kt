package com.baptistaz.taskwave.ui.home.admin.manageprojects.overview

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Allows admin to reassign the manager of a specific project.
 */
class ManageManagerActivity : BaseLocalizedActivity() {

    private lateinit var textProjectTitle: TextView
    private lateinit var textCurrentMgr: TextView
    private lateinit var spinnerMgr: Spinner
    private lateinit var buttonUpdate: Button

    private var managers: List<User> = emptyList()
    private var selected: User? = null
    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_manager)

        // Toolbar setup
        setSupportActionBar(findViewById(R.id.toolbar_assign))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.manage_manager_toolbar_title)

        // View bindings
        textProjectTitle = findViewById(R.id.text_project_title)
        textCurrentMgr = findViewById(R.id.text_current_manager)
        spinnerMgr = findViewById(R.id.spinner_new_manager)
        buttonUpdate = findViewById(R.id.button_update_manager)

        // Get project from intent (required)
        project = intent.getSerializableExtra("project") as? Project ?: return finish()
        textProjectTitle.text = project.name

        // Fetch token and load managers
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()

            // Show current manager
            val current = managers.firstOrNull { it.id_user == project.idManager }
            textCurrentMgr.text = current?.name ?: getString(R.string.manage_manager_current_value)

            // Set up Spinner with manager names
            spinnerMgr.adapter = ArrayAdapter(
                this@ManageManagerActivity,
                android.R.layout.simple_spinner_dropdown_item,
                managers.map { it.name }
            )

            // Preselect current manager
            managers.indexOfFirst { it.id_user == project.idManager }
                .takeIf { it >= 0 }?.let { spinnerMgr.setSelection(it) }

            // Save selected manager
            spinnerMgr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    selected = managers.getOrNull(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Handle update button click
        buttonUpdate.setOnClickListener {
            val mgr = selected ?: return@setOnClickListener

            // Prevent update if same manager selected
            if (mgr.id_user == project.idManager) {
                Toast.makeText(this, getString(R.string.manage_manager_already_assigned), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // Prepare update payload
                    val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
                    val body = ProjectUpdate(
                        id_project = project.idProject,
                        name = project.name,
                        description = project.description ?: "",
                        status = project.status ?: "",
                        start_date = project.startDate ?: "",
                        end_date = project.endDate,
                        id_manager = mgr.id_user
                    )

                    // Send update and return result
                    repo.updateProject(project.idProject, body)
                    val updatedProj = project.copy(idManager = mgr.id_user)
                    setResult(RESULT_OK, Intent().putExtra("project", updatedProj))

                    Toast.makeText(
                        this@ManageManagerActivity,
                        getString(R.string.manage_manager_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@ManageManagerActivity,
                        getString(R.string.manage_manager_error, e.message ?: "Erro desconhecido"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
