package com.baptistaz.taskwave.ui.home.admin.manageprojects

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.model.ProjectUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditProjectActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var buttonEdit: Button

    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        // Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Project"

        // Liga componentes
        inputName = findViewById(R.id.input_name)
        inputDescription = findViewById(R.id.input_description)
        inputStartDate = findViewById(R.id.input_start_date)
        inputEndDate = findViewById(R.id.input_end_date)
        spinnerStatus = findViewById(R.id.spinner_status)
        buttonEdit = findViewById(R.id.button_edit)

        val statusOptions = listOf("active", "completed")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)

        // Recebe o projeto via intent
        project = intent.getSerializableExtra("project") as Project

        // Preenche os dados
        inputName.setText(project.name)
        inputDescription.setText(project.description)
        inputStartDate.setText(project.start_date)
        inputEndDate.setText(project.end_date)
        spinnerStatus.setSelection(statusOptions.indexOf(project.status))

        // Botão de guardar alterações
        buttonEdit.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val repo = ProjectRepository(RetrofitInstance.projectService)

            lifecycleScope.launch {
                try {
                    val start = LocalDate.parse(inputStartDate.text.toString(), formatter)
                    val end = LocalDate.parse(inputEndDate.text.toString(), formatter)

                    val statusFromSpinner = spinnerStatus.selectedItem.toString()
                    val statusCapitalized = statusFromSpinner.replaceFirstChar { it.uppercase() }

                    val updatedProject = ProjectUpdate(
                        id_project = project.id_project,
                        name = inputName.text.toString(),
                        description = inputDescription.text.toString(),
                        status = statusCapitalized,
                        start_date = inputStartDate.text.toString(),
                        end_date = inputEndDate.text.toString()
                    )

                    val gson = com.google.gson.Gson()
                    val jsonBody = gson.toJson(updatedProject)
                    Log.d("PATCH_DEBUG", "JSON enviado: $jsonBody")

                    repo.updateProject(project.id_project, updatedProject)

                    Toast.makeText(this@EditProjectActivity, "Projeto atualizado!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("EDIT_PROJECT_ERROR", "Erro ao atualizar: ${e.message}", e)
                    Toast.makeText(this@EditProjectActivity, "Erro ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
