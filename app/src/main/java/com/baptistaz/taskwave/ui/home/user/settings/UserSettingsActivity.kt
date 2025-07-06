package com.baptistaz.taskwave.ui.home.user.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.data.remote.user.UserRepository
import com.baptistaz.taskwave.ui.home.admin.manageusers.EditUserActivity
import com.baptistaz.taskwave.ui.home.user.base.BaseBottomNavActivity
import com.baptistaz.taskwave.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSettingsActivity : BaseBottomNavActivity() {

    override fun getSelectedMenuId() = R.id.nav_settings

    // Campos da Activity
    private lateinit var progress      : ProgressBar
    private lateinit var contentLayout : LinearLayout
    private lateinit var txtName       : TextView
    private lateinit var imgProfile    : ImageView
    private var currentUserId: String? = null
    private val userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        // Liga views
        progress       = findViewById(R.id.progress_loading)
        contentLayout  = findViewById(R.id.layout_user_settings)
        txtName        = findViewById(R.id.text_name)
        imgProfile     = findViewById(R.id.image_profile)

        // Editar perfil
        findViewById<LinearLayout>(R.id.option_edit_profile).setOnClickListener {
            currentUserId?.let { id ->
                Intent(this, EditUserActivity::class.java)
                    .putExtra("USER_ID", id)
                    .also { startActivity(it) }
            } ?: Toast.makeText(
                this,
                getString(R.string.toast_loading_profile),
                Toast.LENGTH_SHORT
            ).show()
        }

        // ✅ Agora chama o diálogo de idioma
        findViewById<LinearLayout>(R.id.option_change_language).setOnClickListener {
            showLanguageDialog() // método herdado do BaseLocalizedActivity
        }

        // Placeholder - toggle notificações
        findViewById<Switch>(R.id.switch_notifications)
            .setOnCheckedChangeListener { _, isChecked ->
                // TODO: guardar estado
            }

        // Bottom nav (opcional aqui, já é tratado pela BaseBottomNavActivity)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_settings
        bottomNav.setOnItemSelectedListener { true }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        progress.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE

        val token  = SessionManager.getAccessToken(this) ?: return
        val authId = SessionManager.getAuthId(this)     ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val user = userRepo.getUserByAuthId(authId, token)
            progress.visibility = View.GONE

            if (user != null) {
                contentLayout.visibility = View.VISIBLE
                txtName.text = user.name
                currentUserId = user.id_user
            } else {
                Toast.makeText(
                    this@UserSettingsActivity,
                    getString(R.string.error_loading_profile),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
