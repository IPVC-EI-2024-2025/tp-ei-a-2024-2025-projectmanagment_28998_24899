package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
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
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
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
    private lateinit var spinnerManager: Spinner

    private var managers: List<User> = emptyList()
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
        spinnerManager = findViewById(R.id.spinner_manager)

        val statusOptions = listOf("active", "completed")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)

        // Recebe o projeto via intent
        project = intent.getSerializableExtra("project") as Project

        // Preenche os dados
        inputName.setText(project.name)
        inputDescription.setText(project.description)
        inputStartDate.setText(project.startDate)
        inputEndDate.setText(project.endDate)
        spinnerStatus.setSelection(statusOptions.indexOf(project.status))

        // Carregar managers e selecionar o atual
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            val names = managers.map { it.name }
            spinnerManager.adapter = ArrayAdapter(this@EditProjectActivity, android.R.layout.simple_spinner_dropdown_item, names)

            // Seleciona o gestor atual
            val index = managers.indexOfFirst { it.id_user == project.idManager }
            if (index >= 0) spinnerManager.setSelection(index)
        }

        // Botão de guardar alterações
        buttonEdit.setOnClickListener {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val repo = ProjectRepository(RetrofitInstance.projectService)

            val selectedManager = managers.getOrNull(spinnerManager.selectedItemPosition)
            val idManager = selectedManager?.id_user

            lifecycleScope.launch {
                try {
                    val start = LocalDate.parse(inputStartDate.text.toString(), formatter)
                    val end = LocalDate.parse(inputEndDate.text.toString(), formatter)

                    val statusFromSpinner = spinnerStatus.selectedItem.toString()
                    val statusCapitalized = statusFromSpinner.replaceFirstChar { it.uppercase() }

                    val updatedProject = ProjectUpdate(
                        id_project = project.idProject,
                        name = inputName.text.toString(),
                        description = inputDescription.text.toString(),
                        status = statusCapitalized,
                        start_date = inputStartDate.text.toString(),
                        end_date = inputEndDate.text.toString(),
                        id_manager = idManager // <--- NOVO!
                    )

                    val gson = com.google.gson.Gson()
                    val jsonBody = gson.toJson(updatedProject)
                    Log.d("PATCH_DEBUG", "JSON enviado: $jsonBody")

                    repo.updateProject(project.idProject, updatedProject)

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
