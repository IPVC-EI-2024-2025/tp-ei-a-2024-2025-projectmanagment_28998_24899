package com.baptistaz.taskwave.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.auth.LoginActivity
import com.baptistaz.taskwave.utils.SessionManager
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Botão de logout
        findViewById<Button>(R.id.button_logout).setOnClickListener {
            SessionManager.clearAccessToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Botão de mudar idioma
        findViewById<Button>(R.id.button_change_language).setOnClickListener {
            val currentLanguage = SessionManager.getLanguage(this) ?: "en"
            val newLanguage = if (currentLanguage == "en") "pt" else "en"
            SessionManager.saveLanguage(this, newLanguage)
            setLocale(newLanguage)
            recreate()
        }
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}