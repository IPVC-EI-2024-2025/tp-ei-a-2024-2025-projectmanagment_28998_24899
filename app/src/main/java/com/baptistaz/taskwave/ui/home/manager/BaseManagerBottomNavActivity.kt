package com.baptistaz.taskwave.ui.home.manager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baptistaz.taskwave.R
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseManagerBottomNavActivity : AppCompatActivity() {
    abstract fun getSelectedMenuId(): Int

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
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
