package com.baptistaz.taskwave.ui.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.baptistaz.taskwave.R

/**
 * First slide of the onboarding sequence.
 */
class IntroSlider1Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro_slider1, container, false)
    }
}