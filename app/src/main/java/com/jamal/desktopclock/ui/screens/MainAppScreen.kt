package com.jamal.desktopclock.ui.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.jamal.desktopclock.navigation.Screen
import com.jamal.desktopclock.ui.components.CollapsibleSidebar
import com.jamal.desktopclock.ui.theme.AccentBlue
import com.jamal.desktopclock.ui.theme.DarkBackground
import com.jamal.desktopclock.ui.theme.TextPrimary
import com.jamal.desktopclock.ui.theme.TextSecondary
import com.jamal.desktopclock.viewmodel.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var isSidebarExpanded by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Clock) }

    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Calendar permission state
    val calendarPermissionState = rememberPermissionState(
        permission = Manifest.permission.READ_CALENDAR
    )

    // Update permission state in ViewModel
    LaunchedEffect(calendarPermissionState.status.isGranted) {
        viewModel.setCalendarPermission(calendarPermissionState.status.isGranted)
    }

    // Request permission on launch if not granted
    LaunchedEffect(Unit) {
        if (!calendarPermissionState.status.isGranted) {
            calendarPermissionState.launchPermissionRequest()
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Collapsible Sidebar
        CollapsibleSidebar(
            isExpanded = isSidebarExpanded,
            currentScreen = currentScreen,
            onToggle = { isSidebarExpanded = !isSidebarExpanded },
            onNavigate = { screen ->
                currentScreen = screen
                if (screen == Screen.Tasks) {
                    viewModel.refreshEvents()
                }
            }
        )

        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when {
                currentScreen == Screen.Tasks && !calendarPermissionState.status.isGranted -> {
                    // Permission request UI
                    PermissionRequestScreen(
                        onRequestPermission = { calendarPermissionState.launchPermissionRequest() },
                        shouldShowRationale = calendarPermissionState.status.shouldShowRationale
                    )
                }
                currentScreen == Screen.Tasks && isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
                else -> {
                    when (currentScreen) {
                        Screen.Clock -> ClockScreen()
                        Screen.Tasks -> TasksScreen(events = events)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    shouldShowRationale: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (shouldShowRationale) {
                    "Calendar permission is required to display your tasks"
                } else {
                    "Grant calendar permission to see your tasks"
                },
                color = TextSecondary,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Grant Permission",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            }
        }
    }
}
