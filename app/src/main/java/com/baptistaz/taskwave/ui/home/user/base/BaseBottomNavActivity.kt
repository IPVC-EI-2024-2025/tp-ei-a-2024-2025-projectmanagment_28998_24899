package com.baptistaz.taskwave.ui.home.user.base

import android.content.Intent
import android.os.Bundle
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.ui.home.user.home.UserHomeActivity
import com.baptistaz.taskwave.ui.home.user.settings.UserProfileActivity
import com.baptistaz.taskwave.ui.home.user.settings.UserSettingsActivity
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Abstract base class for user screens that use bottom navigation.
 * Automatically handles language support and navigation behavior.
 */
abstract class BaseBottomNavActivity : BaseLocalizedActivity() {

    abstract fun getSelectedMenuId(): Int

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Initialize bottom navigation and highlight the current active item
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.selectedItemId = getSelectedMenuId()

        // Handle navigation item selection
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Navigate to Profile screen if it's not the current one
                R.id.nav_profile -> {
                    if (getSelectedMenuId() != R.id.nav_profile) {
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        overridePendingTransition(0, 0) // No animation
                        finish()
                    }
                    true
                }
                // """"" Home screen """""
                R.id.nav_home -> {
                    if (getSelectedMenuId() != R.id.nav_home) {
                        startActivity(Intent(this, UserHomeActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                // """"" Settings screen """""
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
