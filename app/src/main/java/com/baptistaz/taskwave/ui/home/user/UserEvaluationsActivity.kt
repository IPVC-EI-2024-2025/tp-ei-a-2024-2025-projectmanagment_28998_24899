package com.baptistaz.taskwave.ui.home.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.manager.EvaluationRepository
import com.baptistaz.taskwave.utils.SessionManager
import kotlinx.coroutines.launch

class UserEvaluationsActivity : AppCompatActivity() {

    private lateinit var rvEvals: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_evaluations)

        // toolbar com back
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }

        rvEvals = findViewById(R.id.rv_user_evaluations)

        val token  = SessionManager.getAccessToken(this) ?: return finish()
        val userId = SessionManager.getUserId(this)      ?: return finish()

        lifecycleScope.launch {
            try {
                // 1) buscar avaliações onde id_user = meu userId
                val evals = EvaluationRepository(token)
                    .getEvaluationsByUser(userId)

                // 2) popular RecyclerView
                rvEvals.layoutManager = LinearLayoutManager(this@UserEvaluationsActivity)
                rvEvals.adapter       = UserEvaluationAdapter(evals)
            }
            catch (e: Exception) {
                Toast.makeText(this@UserEvaluationsActivity,
                    "Não foi possível carregar avaliações: ${e.message}",
                    Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
