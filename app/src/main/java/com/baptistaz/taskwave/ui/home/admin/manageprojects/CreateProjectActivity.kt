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
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CreateProjectActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputDescription: EditText
    private lateinit var inputStartDate: EditText
    private lateinit var inputEndDate: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var buttonCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        // Toolbar de voltar atrás
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Project"

        // Ligação aos componentes
        inputName = findViewById(R.id.input_name)
        inputDescription = findViewById(R.id.input_description)
        inputStartDate = findViewById(R.id.input_start_date)
        inputEndDate = findViewById(R.id.input_end_date)
        spinnerStatus = findViewById(R.id.spinner_status)
        buttonCreate = findViewById(R.id.button_create_project)

        // Setup do spinner
        val statusOptions = listOf("active", "completed")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)

        // Clique no botão
        buttonCreate.setOnClickListener {
            val name = inputName.text.toString()
            val description = inputDescription.text.toString()
            val status = spinnerStatus.selectedItem.toString().replaceFirstChar { it.uppercase() }
            val startDate = inputStartDate.text.toString()
            val endDate = inputEndDate.text.toString()

            val project = Project(
                id_project = UUID.randomUUID().toString(),
                name = name,
                description = description,
                status = status,
                start_date = startDate,
                end_date = endDate
            )

            // Enviar para Supabase
            val repository = ProjectRepository(RetrofitInstance.projectService)
            lifecycleScope.launch {
                try {
                    Log.d("CREATE_PROJECT", "Enviando projeto: $project")

                    val result = repository.createProject(project)

                    Log.d("CREATE_PROJECT", "Projeto criado com sucesso: $result")

                    Toast.makeText(this@CreateProjectActivity, "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Voltar atrás
                } catch (e: Exception) {
                    Log.e("CREATE_PROJECT", "Erro ao criar projeto: ${e.message}", e)
                    Toast.makeText(this@CreateProjectActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
