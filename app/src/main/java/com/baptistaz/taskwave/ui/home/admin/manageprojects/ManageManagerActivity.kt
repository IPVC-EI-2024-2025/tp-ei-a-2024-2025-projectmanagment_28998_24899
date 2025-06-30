package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
import android.content.Intent
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

    private lateinit var textProjectTitle : TextView
    private lateinit var textCurrentMgr   : TextView
    private lateinit var spinnerMgr       : Spinner
    private lateinit var buttonUpdate     : Button

    private var managers : List<User> = emptyList()
    private var selected : User?      = null
    private lateinit var project      : Project        // agora non-null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_manager)

        /* toolbar back */
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textProjectTitle = findViewById(R.id.text_project_title)
        textCurrentMgr   = findViewById(R.id.text_current_manager)
        spinnerMgr       = findViewById(R.id.spinner_new_manager)
        buttonUpdate     = findViewById(R.id.button_update_manager)

        /* ----- projecto recebido ----- */
        project = intent.getSerializableExtra("project") as? Project
            ?: return finish()               // safety

        textProjectTitle.text = project.name

        /* ----- carrega gestores ----- */
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()

            val current = managers.firstOrNull { it.id_user == project.idManager }
            textCurrentMgr.text = current?.name ?: "No manager"

            spinnerMgr.adapter = ArrayAdapter(
                this@ManageManagerActivity,
                android.R.layout.simple_spinner_dropdown_item,
                managers.map { it.name }
            )
            /* pré-selecciona */
            managers.indexOfFirst { it.id_user == project.idManager }
                .takeIf { it >= 0 }?.let { spinnerMgr.setSelection(it) }

            spinnerMgr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                    selected = managers.getOrNull(pos)
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }

        /* ----- PATCH + devolver resultado ----- */
        buttonUpdate.setOnClickListener {
            val mgr = selected ?: return@setOnClickListener
            if (mgr.id_user == project.idManager) {          // nada mudou
                Toast.makeText(this, "Já está atribuído a esse gestor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
                    val body = ProjectUpdate(
                        id_project  = project.idProject,
                        name        = project.name,
                        description = project.description,
                        status      = project.status,
                        start_date  = project.startDate,
                        end_date    = project.endDate,
                        id_manager  = mgr.id_user
                    )
                    repo.updateProject(project.idProject, body)

                    /* constrói versão actualizada para devolver */
                    val updatedProj = project.copy(idManager = mgr.id_user)
                    setResult(RESULT_OK, Intent().putExtra("project", updatedProj))
                    Toast.makeText(this@ManageManagerActivity, "Manager actualizado!", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ManageManagerActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
