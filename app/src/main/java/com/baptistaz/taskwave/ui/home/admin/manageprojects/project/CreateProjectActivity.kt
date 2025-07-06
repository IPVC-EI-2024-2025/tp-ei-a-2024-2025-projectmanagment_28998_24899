package com.baptistaz.taskwave.ui.home.admin.manageprojects.project

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.auth.User
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

/**
 * Admin screen to create a new project.
 * Allows selecting a manager, picking start/end dates, and submitting a new project to the database.
 */
class CreateProjectActivity : BaseLocalizedActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var buttonCreate: Button
    private lateinit var spinnerManager: Spinner

    private var managers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        // Set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.create_project_toolbar_title)

        // Bind views
        inputName = findViewById(R.id.input_name)
        inputDescription = findViewById(R.id.input_description)
        inputStartDate = findViewById(R.id.input_start_date)
        inputEndDate = findViewById(R.id.input_end_date)
        buttonCreate = findViewById(R.id.button_create_project)
        spinnerManager = findViewById(R.id.spinner_manager)

        // Show date picker for start date
        inputStartDate.setOnClickListener {
            showDatePicker { selectedDate ->
                inputStartDate.setText(selectedDate)
            }
        }

        // Show date picker for end date
        inputEndDate.setOnClickListener {
            showDatePicker { selectedDate ->
                inputEndDate.setText(selectedDate)
            }
        }

        // Load available managers
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            Log.d("CREATE_PROJECT", "Managers loaded: ${managers.size} - $managers")

            val names = managers.map { it.name }
            spinnerManager.adapter = ArrayAdapter(
                this@CreateProjectActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        // Handle "Create Project" button click
        buttonCreate.setOnClickListener {
            val name = inputName.text.toString()
            val description = inputDescription.text.toString()
            val status = "Active"
            val startDate = inputStartDate.text.toString()
            val endDate = inputEndDate.text.toString()
            val selectedManager = managers.getOrNull(spinnerManager.selectedItemPosition)
            val idManager = selectedManager?.id_user

            val project = Project(
                idProject = UUID.randomUUID().toString(),
                name = name,
                description = description,
                status = status,
                startDate = startDate,
                endDate = endDate,
                idManager = idManager
            )

            val repository = ProjectRepository(RetrofitInstance.projectService)

            lifecycleScope.launch {
                try {
                    Log.d("CREATE_PROJECT", "Sending project: $project")
                    repository.createProject(project)
                    Toast.makeText(
                        this@CreateProjectActivity,
                        getString(R.string.create_project_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("CREATE_PROJECT", "Error creating project: ${e.message}", e)
                    Toast.makeText(
                        this@CreateProjectActivity,
                        getString(R.string.create_project_error, e.message ?: "Unknown error"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Shows a calendar dialog and returns the selected date formatted as yyyy-MM-dd.
     */
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val formatted = "%04d-%02d-%02d".format(y, m + 1, d)
            onDateSelected(formatted)
        }, year, month, day)

        datePicker.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
