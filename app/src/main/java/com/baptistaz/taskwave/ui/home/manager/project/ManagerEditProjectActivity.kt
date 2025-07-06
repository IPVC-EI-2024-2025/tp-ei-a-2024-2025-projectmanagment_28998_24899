package com.baptistaz.taskwave.ui.home.manager.project

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.project.Project
import com.baptistaz.taskwave.data.model.project.ProjectUpdate
import com.baptistaz.taskwave.data.remote.common.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.repository.ProjectRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Manager screen for editing an existing project.
 */
class ManagerEditProjectActivity : BaseLocalizedActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var buttonEdit: Button

    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_edit_project)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_edit_project)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_edit_project)

        // Initialize UI fields
        inputName = findViewById(R.id.input_name)
        inputDescription = findViewById(R.id.input_description)
        inputStartDate = findViewById(R.id.input_start_date)
        inputEndDate = findViewById(R.id.input_end_date)
        buttonEdit = findViewById(R.id.button_edit)

        // Get project passed via intent
        project = intent.getSerializableExtra("project") as Project

        // Fill fields with existing project data
        inputName.setText(project.name)
        inputDescription.setText(project.description)
        inputStartDate.setText(project.startDate)
        inputEndDate.setText(project.endDate)

        // Open date picker on date fields
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

        // Handle project update
        buttonEdit.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val repo = ProjectRepository(RetrofitInstance.projectService)

            lifecycleScope.launch {
                try {
                    // Validate date format
                    LocalDate.parse(inputStartDate.text.toString(), formatter)
                    LocalDate.parse(inputEndDate.text.toString(), formatter)

                    val statusFormatted = project.status ?: "Active"

                    // Create updated project object
                    val updatedProject = ProjectUpdate(
                        id_project = project.idProject,
                        name = inputName.text.toString(),
                        description = inputDescription.text.toString(),
                        status = statusFormatted,
                        start_date = inputStartDate.text.toString(),
                        end_date = inputEndDate.text.toString(),
                        id_manager = project.idManager
                    )

                    // Debug log to verify sent JSON
                    val gson = com.google.gson.Gson()
                    val jsonBody = gson.toJson(updatedProject)
                    Log.d("PATCH_DEBUG", "JSON sent: $jsonBody")

                    // Send update to backend
                    repo.updateProject(project.idProject, updatedProject)

                    Toast.makeText(
                        this@ManagerEditProjectActivity,
                        getString(R.string.project_updated_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                } catch (e: Exception) {
                    Log.e("EDIT_PROJECT_ERROR", "Update error: ${e.message}", e)
                    Toast.makeText(
                        this@ManagerEditProjectActivity,
                        "Update failed: ${e.message}", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Shows a date picker and returns the selected date in yyyy-MM-dd format.
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
