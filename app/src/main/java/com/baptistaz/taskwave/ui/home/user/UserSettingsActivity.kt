package com.baptistaz.taskwave.ui.home.user

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.UserRepository
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSettingsActivity : BaseBottomNavActivity() {
    override fun getSelectedMenuId(): Int = R.id.nav_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        val progress = findViewById<ProgressBar>(R.id.progress_loading)
        val contentLayout = findViewById<LinearLayout>(R.id.layout_user_settings)
        val txtName = findViewById<TextView>(R.id.text_name)
        val imgProfile = findViewById<ImageView>(R.id.image_profile)


        progress.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        // Ir buscar o user autenticado ao backend, tal como já fazes nas outras páginas!
        val token = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val user = UserRepository().getUserByAuthId(authId, token)
            progress.visibility = View.GONE
            if (user != null) {
                contentLayout.visibility = View.VISIBLE
                txtName.text = user.name ?: ""
                // Podes pôr foto, etc...
            }
        }

        // Ligar listeners das opções (vai ser útil nos próximos passos)
        findViewById<LinearLayout>(R.id.option_edit_profile).setOnClickListener {
            // Abrir Activity de editar perfil
        }
        findViewById<LinearLayout>(R.id.option_change_password).setOnClickListener {
            // Abrir Activity de mudar password
        }
        findViewById<LinearLayout>(R.id.option_change_language).setOnClickListener {
            // Abrir diálogo de linguagens
        }
        findViewById<Switch>(R.id.switch_notifications).setOnCheckedChangeListener { _, isChecked ->
            // Guardar estado da notificação (em settings/local)
        }

        // Bottom Navigation (mantém igual)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_settings
        bottomNav.setOnItemSelectedListener { item ->
            // ... tua navegação já feita
            true
        }
    }
}
