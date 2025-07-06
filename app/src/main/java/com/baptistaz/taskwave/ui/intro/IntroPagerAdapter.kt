package com.baptistaz.taskwave.ui.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Pager adapter for the intro screen ViewPager2.
 * Displays a sequence of intro fragments.
 *
 * @param activity The hosting activity.
 * @param fragments List of fragments to display.
 */
class IntroPagerAdapter(
    activity: FragmentActivity,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}