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
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Admin screen to edit an existing project.
 * Submits the updated information to the backend via PATCH request.
 */
class EditProjectActivity : BaseLocalizedActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var buttonEdit: Button
    private lateinit var spinnerManager: Spinner

    private var managers: List<User> = emptyList()
    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        // Set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_edit_project)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.edit_project_toolbar_title)

        // Bind views
        inputName = findViewById(R.id.input_name)
        inputDescription = findViewById(R.id.input_description)
        inputStartDate = findViewById(R.id.input_start_date)
        inputEndDate = findViewById(R.id.input_end_date)
        buttonEdit = findViewById(R.id.button_edit)
        spinnerManager = findViewById(R.id.spinner_manager)

        // Get project passed via intent
        project = intent.getSerializableExtra("project") as Project

        // Populate fields with existing project data
        inputName.setText(project.name)
        inputDescription.setText(project.description)
        inputStartDate.setText(project.startDate)
        inputEndDate.setText(project.endDate)

        // Enable date pickers
        inputStartDate.setOnClickListener {
            showDatePicker(inputStartDate.text.toString()) { selected ->
                inputStartDate.setText(selected)
            }
        }

        inputEndDate.setOnClickListener {
            showDatePicker(inputEndDate.text.toString()) { selected ->
                inputEndDate.setText(selected)
            }
        }

        // Load manager list into spinner
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            val names = managers.map { it.name }
            spinnerManager.adapter = ArrayAdapter(
                this@EditProjectActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )

            val index = managers.indexOfFirst { it.id_user == project.idManager }
            if (index >= 0) spinnerManager.setSelection(index)
        }

        // Handle update button click
        buttonEdit.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val repo = ProjectRepository(RetrofitInstance.projectService)

            val selectedManager = managers.getOrNull(spinnerManager.selectedItemPosition)
            val idManager = selectedManager?.id_user

            lifecycleScope.launch {
                try {
                    // Validate date format
                    LocalDate.parse(inputStartDate.text.toString(), formatter)
                    LocalDate.parse(inputEndDate.text.toString(), formatter)

                    // Create update object
                    val updatedProject = ProjectUpdate(
                        id_project = project.idProject,
                        name = inputName.text.toString(),
                        description = inputDescription.text.toString(),
                        status = project.status ?: "",
                        start_date = inputStartDate.text.toString(),
                        end_date = inputEndDate.text.toString(),
                        id_manager = idManager
                    )

                    // Log JSON
                    val gson = com.google.gson.Gson()
                    Log.d("PATCH_DEBUG", "JSON sent: ${gson.toJson(updatedProject)}")

                    // Send update
                    repo.updateProject(project.idProject, updatedProject)

                    Toast.makeText(
                        this@EditProjectActivity,
                        getString(R.string.edit_project_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("EDIT_PROJECT_ERROR", "Error updating: ${e.message}", e)
                    Toast.makeText(
                        this@EditProjectActivity,
                        getString(R.string.edit_project_error, e.message ?: "Unknown error"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Shows a DatePickerDialog and returns selected date as yyyy-MM-dd.
     */
    private fun showDatePicker(initialDate: String?, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        if (!initialDate.isNullOrBlank()) {
            try {
                val parts = initialDate.split("-")
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } catch (_: Exception) {}
        }

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
