package com.baptistaz.taskwave.ui.intro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.baptistaz.taskwave.MainActivity
import com.baptistaz.taskwave.R
import com.baptistaz.taskwave.utils.BaseLocalizedActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Onboarding activity with slider fragments.
 */
class IntroActivity : BaseLocalizedActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Setup ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        val fragments = listOf(
            IntroSlider1Fragment(),
            IntroSlider2Fragment(),
            IntroSlider3Fragment()
        )

        val adapter = IntroPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Advance to next page or launch MainActivity when done
        findViewById<Button>(R.id.button_next).setOnClickListener {
            if (viewPager.currentItem < fragments.size - 1) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}
