package com.baptistaz.taskwave.ui.home.admin.manageprojects

import User
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.Project
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.data.remote.project.ProjectRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var textName      : TextView
    private lateinit var textManager   : TextView
    private lateinit var textDesc      : TextView
    private lateinit var textStatus    : TextView
    private lateinit var textStartDate : TextView
    private lateinit var textEndDate   : TextView
    private lateinit var buttonTasks   : Button
    private lateinit var buttonMgr     : Button
    private lateinit var buttonDone    : Button

    private var managers : List<User> = emptyList()
    private lateinit var project      : Project   // guarda sempre a versão actual

    /* recebe resultado da ManageManagerActivity */
    private val manageMgrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) {
            val upd = res.data?.getSerializableExtra("project") as? Project
            upd?.let { project = it; atualizarUI(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* refs */
        textName       = findViewById(R.id.text_project_name)
        textManager    = findViewById(R.id.text_manager)
        textDesc       = findViewById(R.id.text_project_description)
        textStatus     = findViewById(R.id.text_project_status)
        textStartDate  = findViewById(R.id.text_project_start)
        textEndDate    = findViewById(R.id.text_project_end)
        buttonTasks    = findViewById(R.id.button_view_tasks)
        buttonMgr      = findViewById(R.id.button_manage_manager)
        buttonDone     = findViewById(R.id.button_mark_complete)

        /* projecto inicial */
        project = intent.getSerializableExtra("project") as? Project
            ?: return finish()

        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            managers = UserRepository().getAllManagers(token) ?: emptyList()
            atualizarUI(project)
        }

        buttonTasks.setOnClickListener {
            startActivity(Intent(this, ProjectTasksActivity::class.java)
                .putExtra("project_id", project.idProject))
        }

        buttonMgr.setOnClickListener {
            manageMgrLauncher.launch(
                Intent(this, ManageManagerActivity::class.java)
                    .putExtra("project", project)
            )
        }

        buttonDone.setOnClickListener {
            Toast.makeText(this, "Em breve!", Toast.LENGTH_SHORT).show()
        }
    }

    /* Re-sync (caso haja alterações fora deste fluxo) */
    override fun onResume() {
        super.onResume()
        val token = SessionManager.getAccessToken(this) ?: return
        lifecycleScope.launch {
            val repo = ProjectRepository(RetrofitInstance.getProjectService(token))
            repo.getProjectById(project.idProject)?.let {
                project = it
                atualizarUI(it)
            }
        }
    }

    private fun atualizarUI(p: Project) {
        textName.text      = p.name
        textDesc.text      = p.description
        textStatus.text    = p.status
        textStartDate.text = p.startDate
        textEndDate.text   = p.endDate
        val mgrName = managers.firstOrNull { it.id_user == p.idManager }?.name ?: "No manager"
        textManager.text = "Manager: $mgrName"
    }

    override fun onSupportNavigateUp(): Boolean = finish().let { true }
}
