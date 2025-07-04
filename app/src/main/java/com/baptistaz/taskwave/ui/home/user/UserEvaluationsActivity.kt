package com.baptistaz.taskwave.ui.home.user

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.manager.EvaluationRepository
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class UserEvaluationsActivity : BaseLocalizedActivity() {

    private lateinit var rvEvals: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_evaluations)

        // toolbar com título e botão "voltar"
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_user_evaluations)

        toolbar.setNavigationOnClickListener { finish() }

        rvEvals = findViewById(R.id.rv_user_evaluations)

        val token  = SessionManager.getAccessToken(this) ?: return finish()
        val userId = SessionManager.getUserId(this)      ?: return finish()

        lifecycleScope.launch {
            try {
                val evals = EvaluationRepository(token).getEvaluationsByUser(userId)
                rvEvals.layoutManager = LinearLayoutManager(this@UserEvaluationsActivity)
                rvEvals.adapter       = UserEvaluationAdapter(evals)
            }
            catch (e: Exception) {
                Toast.makeText(
                    this@UserEvaluationsActivity,
                    getString(R.string.error_loading_evaluations, e.message ?: "erro desconhecido"),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
}
