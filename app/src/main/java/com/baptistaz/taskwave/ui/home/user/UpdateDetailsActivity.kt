package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class UpdateDetailsActivity : AppCompatActivity() {

    private lateinit var repo: TaskUpdateRepository
    private lateinit var upd : TaskUpdate

    // -------------------------------- life-cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        bind(upd)                               // primeira vez
        initButtons()
    }

    override fun onResume() {
        super.onResume()
        // Re-buscar caso tenha sido editado
        lifecycleScope.launch {
            upd = repo.getById(upd.idUpdate!!)   // adiciona método no repo
            bind(upd)
            setResult(RESULT_OK)                 // timeline refresca se necessário
        }
    }

    // -------------------------------- UI helpers
    private fun bind(u: TaskUpdate) = with(u) {
        findViewById<TextView>(R.id.text_title).text = title
        findViewById<TextView>(R.id.text_date ).text = date?.take(10) ?: ""
        findViewById<TextView>(R.id.text_notes).text = notes ?: "-"
        findViewById<TextView>(R.id.text_loc  ).text = location ?: "-"
        findViewById<TextView>(R.id.text_time ).text = timeSpent ?: "-"
    }

    private fun initButtons() {
        findViewById<MaterialButton>(R.id.btn_edit).setOnClickListener {
            startActivity(Intent(this, EditUpdateActivity::class.java).putExtra("UPDATE", upd))
        }
        findViewById<MaterialButton>(R.id.btn_delete).setOnClickListener {
            lifecycleScope.launch {
                repo.delete(upd.idUpdate!!)
                Toast.makeText(this@UpdateDetailsActivity,"Update deleted",Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp() = finish().let { true }
}


