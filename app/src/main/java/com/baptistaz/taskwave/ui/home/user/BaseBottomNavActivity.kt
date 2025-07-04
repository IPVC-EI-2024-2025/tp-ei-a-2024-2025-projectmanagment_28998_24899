package com.baptistaz.taskwave.ui.home.user

import android.content.Intent
import android.os.Bundle
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseBottomNavActivity : BaseLocalizedActivity() {  // <- herda BaseLocalizedActivity
    abstract fun getSelectedMenuId(): Int

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState) // já chama o super do BaseLocalizedActivity

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = getSelectedMenuId()

        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    if (getSelectedMenuId() != R.id.nav_profile) {
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_home -> {
                    if (getSelectedMenuId() != R.id.nav_home) {
                        startActivity(Intent(this, UserHomeActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_settings -> {
                    if (getSelectedMenuId() != R.id.nav_settings) {
                        startActivity(Intent(this, UserSettingsActivity::class.java))
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
