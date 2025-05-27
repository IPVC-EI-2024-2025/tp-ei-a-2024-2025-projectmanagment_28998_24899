package com.baptistaz.taskwave

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.baptistaz.taskwave.data.remote.UserRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val response = UserRepository().getAllUsers()
                if (response.isSuccessful) {
                    Log.d("SUPABASE", "Utilizadores: ${response.body()}")
                } else {
                    Log.e("SUPABASE", "Erro: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SUPABASE", "Exceção: ${e.message}")
            }
        }

        setContent {
            // UI futura
        }
    }
}