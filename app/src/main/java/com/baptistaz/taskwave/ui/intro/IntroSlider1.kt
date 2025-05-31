package com.baptistaz.taskwave.ui.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.baptistaz.taskwave.R

@Composable
fun IntroSlider1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8E5A0)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.taskwave_logo),
            contentDescription = "TaskWave Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Image(
            painter = painterResource(R.drawable.woman_laptop),
            contentDescription = "Efficient Task Management",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Efficient Task Management",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1A3C5E)
        )
        Text(
            text = "Stay organized and productive",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF1A3C5E)
        )
    }
}