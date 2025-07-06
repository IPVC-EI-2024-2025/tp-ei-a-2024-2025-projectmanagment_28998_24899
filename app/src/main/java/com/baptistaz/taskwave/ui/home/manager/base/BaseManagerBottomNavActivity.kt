package com.baptistaz.taskwave.ui.home.manager.base
import android.content.Intent
import android.os.Bundle
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.home.manager.settings.ManagerProfileActivity
import com.baptistaz.taskwave.ui.home.manager.project.list.ManagerProjectsAreaActivity
import com.baptistaz.taskwave.ui.home.manager.settings.ManagerSettingsActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseManagerBottomNavActivity : BaseLocalizedActivity() {

    abstract fun getSelectedMenuId(): Int

    override fun onPostCreate(savedInstanceState: Bundle?) {
        // ✅ chama primeiro a super (BaseLocalizedActivity.onPostCreate)
        super.onPostCreate(savedInstanceState)

        // Depois trata da bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = getSelectedMenuId()
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    if (getSelectedMenuId() != R.id.nav_profile) {
                        startActivity(Intent(this, ManagerProfileActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_manager_area -> {
                    if (getSelectedMenuId() != R.id.nav_manager_area) {
                        startActivity(Intent(this, ManagerProjectsAreaActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_settings -> {
                    if (getSelectedMenuId() != R.id.nav_settings) {
                        startActivity(Intent(this, ManagerSettingsActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
}

