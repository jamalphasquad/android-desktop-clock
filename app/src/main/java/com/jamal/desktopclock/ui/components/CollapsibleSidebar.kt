package com.jamal.desktopclock.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamal.desktopclock.navigation.Screen
import com.jamal.desktopclock.ui.theme.AccentBlue
import com.jamal.desktopclock.ui.theme.DarkSurface
import com.jamal.desktopclock.ui.theme.TaskCardBackground
import com.jamal.desktopclock.ui.theme.TextPrimary
import com.jamal.desktopclock.ui.theme.TextSecondary

data class SidebarItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val sidebarItems = listOf(
    SidebarItem(Screen.Clock, Icons.Default.AccessTime, "Clock"),
    SidebarItem(Screen.Tasks, Icons.Default.CalendarToday, "Tasks")
)

@Composable
fun CollapsibleSidebar(
    isExpanded: Boolean,
    currentScreen: Screen,
    onToggle: () -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 72.dp,
        animationSpec = tween(300),
        label = "sidebarWidth"
    )

    Surface(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = DarkSurface,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = if (isExpanded) Alignment.Start else Alignment.CenterHorizontally
        ) {
            // Toggle button
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(if (isExpanded) Alignment.End else Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = if (isExpanded)
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    else
                        Icons.Default.Menu,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation items
            sidebarItems.forEach { item ->
                SidebarNavigationItem(
                    item = item,
                    isSelected = currentScreen == item.screen,
                    isExpanded = isExpanded,
                    onClick = { onNavigate(item.screen) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SidebarNavigationItem(
    item: SidebarItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent
    val contentColor = if (isSelected) AccentBlue else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    }
}
