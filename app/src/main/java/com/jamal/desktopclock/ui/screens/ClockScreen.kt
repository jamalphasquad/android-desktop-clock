package com.jamal.desktopclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamal.desktopclock.ui.theme.BlackColor
import com.jamal.desktopclock.ui.theme.DarkBackground
import com.jamal.desktopclock.ui.theme.DarkSurface
import com.jamal.desktopclock.ui.theme.TextPrimary
import com.jamal.desktopclock.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun ClockScreen(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val secondsFormatter = remember { DateTimeFormatter.ofPattern("ss") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlackColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main time display
            Text(
                text = currentTime.format(timeFormatter),
                fontSize = 340.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 8.sp
            )

            // Seconds
//            Text(
//                text = currentTime.format(secondsFormatter),
//                fontSize = 48.sp,
//                fontWeight = FontWeight.Medium,
//                color = TextSecondary,
//                modifier = Modifier.padding(top = 8.dp)
//            )

            // Date
            Text(
                text = currentTime.format(dateFormatter),
                fontSize = 48.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}
