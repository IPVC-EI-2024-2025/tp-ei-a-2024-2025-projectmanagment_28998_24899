package com.baptistaz.taskwave

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.data.remote.RetrofitInstance
import com.baptistaz.taskwave.data.remote.auth.AuthRepository
import com.baptistaz.taskwave.ui.auth.AuthViewModel
import com.baptistaz.taskwave.ui.auth.AuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val service = RetrofitInstance.auth
        val repository = AuthRepository(service)
        AuthViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("SUPABASE", "AuthViewModel inicializado: $authViewModel")

        lifecycleScope.launch {
            authViewModel.authResponse.collectLatest { response ->
                response?.let {
                    if (it.isSuccessful) {
                        val token = it.body()?.access_token
                        if (!token.isNullOrEmpty()) {
                            Log.d("AUTH", "✅ Login OK! Token: $token")
                        } else {
                            Log.w("AUTH", "⚠️ Registo OK, mas sem token (faz login em seguida)")
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            authViewModel.signup("teste@teste.com", "12345678")
            // login será feito manualmente depois, quando criarmos a UI
        }

        setContent {
            // UI futura
        }
    }
}
