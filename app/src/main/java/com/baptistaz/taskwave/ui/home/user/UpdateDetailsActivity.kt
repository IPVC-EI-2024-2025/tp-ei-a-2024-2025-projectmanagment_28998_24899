package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.model.TaskUpdate
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.project.TaskUpdateRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class UpdateDetailsActivity : BaseLocalizedActivity() {

    private lateinit var repo: TaskUpdateRepository
    private lateinit var upd: TaskUpdate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        setSupportActionBar(findViewById(R.id.toolbar_update_detail))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.update_details)

        upd = intent.getSerializableExtra("UPDATE") as TaskUpdate
        val token = SessionManager.getAccessToken(this) ?: return finish()
        repo = TaskUpdateRepository(RetrofitInstance.getTaskUpdateService(token))

        bind(upd)
        initButtons()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            upd = repo.getById(upd.idUpdate!!)
            bind(upd)
            setResult(RESULT_OK)
        }
    }

    private fun bind(u: TaskUpdate) = with(u) {
        findViewById<TextView>(R.id.text_title).text = title
        findViewById<TextView>(R.id.text_date).text = date?.take(10) ?: ""
        findViewById<TextView>(R.id.text_notes).text = notes ?: "-"
        findViewById<TextView>(R.id.text_loc).text = location ?: "-"
        findViewById<TextView>(R.id.text_time).text = timeSpent ?: "-"
    }

    private fun initButtons() {
        findViewById<MaterialButton>(R.id.btn_edit).apply {
            text = getString(R.string.btn_edit_update)
            setOnClickListener {
                startActivity(
                    Intent(this@UpdateDetailsActivity, EditUpdateActivity::class.java)
                        .putExtra("UPDATE", upd)
                )
            }
        }

        findViewById<MaterialButton>(R.id.btn_delete).apply {
            text = getString(R.string.btn_delete)
            setOnClickListener {
                lifecycleScope.launch {
                    repo.delete(upd.idUpdate!!)
                    Toast.makeText(
                        this@UpdateDetailsActivity,
                        getString(R.string.toast_update_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
