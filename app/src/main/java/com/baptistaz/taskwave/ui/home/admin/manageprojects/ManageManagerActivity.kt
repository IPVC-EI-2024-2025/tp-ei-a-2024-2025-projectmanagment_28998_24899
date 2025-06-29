package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.model.ProjectUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ManageManagerActivity : AppCompatActivity() {

    private lateinit var textProjectTitle: TextView
    private lateinit var textCurrentManager: TextView
    private lateinit var spinnerNewManager: Spinner
    private lateinit var buttonUpdateManager: Button

    private var managers: List<User> = emptyList()
    private var selectedManager: User? = null
    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_manager)

        textProjectTitle = findViewById(R.id.text_project_title)
        textCurrentManager = findViewById(R.id.text_current_manager)
        spinnerNewManager = findViewById(R.id.spinner_new_manager)
        buttonUpdateManager = findViewById(R.id.button_update_manager)

        // Toolbar Back
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Recebe o projeto
        project = intent.getSerializableExtra("project") as? Project
        val token = SessionManager.getAccessToken(this) ?: return

        project?.let { proj ->
            textProjectTitle.text = proj.name

            lifecycleScope.launch {
                managers = UserRepository().getAllManagers(token) ?: emptyList()

                // Nome do manager atual
                val currentManager = managers.firstOrNull { it.id_user == proj.idManager }
                textCurrentManager.text = currentManager?.name ?: "No manager"

                // Spinner com todos os managers
                val managerNames = managers.map { it.name }
                spinnerNewManager.adapter = ArrayAdapter(this@ManageManagerActivity, android.R.layout.simple_spinner_dropdown_item, managerNames)

                // Se já existe manager, seleciona-o
                val currentIdx = managers.indexOfFirst { it.id_user == proj.idManager }
                if (currentIdx >= 0) spinnerNewManager.setSelection(currentIdx)

                spinnerNewManager.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        selectedManager = managers.getOrNull(position)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                })
            }
        }

        buttonUpdateManager.setOnClickListener {
            val proj = project ?: return@setOnClickListener
            val manager = selectedManager ?: return@setOnClickListener
            val token = SessionManager.getAccessToken(this) ?: return@setOnClickListener

            lifecycleScope.launch {
                try {
                    val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
                    val update = ProjectUpdate(
                        id_project = proj.idProject,
                        name = proj.name,
                        description = proj.description,
                        status = proj.status,
                        start_date = proj.startDate,
                        end_date = proj.endDate,
                        id_manager = manager.id_user
                    )
                    repo.updateProject(proj.idProject, update)
                    Toast.makeText(this@ManageManagerActivity, "Manager updated!", Toast.LENGTH_SHORT).show()
                    finish() // Ou podes dar NavUtils.navigateUpFromSameTask(this@ManageManagerActivity)
                } catch (e: Exception) {
                    Toast.makeText(this@ManageManagerActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
