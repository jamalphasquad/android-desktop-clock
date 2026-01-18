package com.jamal.desktopclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamal.desktopclock.data.CalendarEvent
import com.jamal.desktopclock.ui.theme.DarkBackground
import com.jamal.desktopclock.ui.theme.TaskCardBackground
import com.jamal.desktopclock.ui.theme.TextPrimary
import com.jamal.desktopclock.ui.theme.TextSecondary
import com.jamal.desktopclock.ui.theme.TimelineBlue
import com.jamal.desktopclock.ui.theme.TimelineDot
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Height per hour in dp
private val HOUR_HEIGHT = 80.dp

@Composable
fun TasksScreen(
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Auto-scroll to current time on first load
    LaunchedEffect(Unit) {
        val currentHour = LocalDateTime.now().hour
        val scrollPosition = with(density) { (HOUR_HEIGHT * currentHour).toPx().toInt() }
        scrollState.animateScrollTo(scrollPosition - 100) // Scroll a bit above current time
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(60000) // Update every minute
        }
    }

    // Sort all events by start time
    val sortedEvents = events.sortedBy { it.startTime }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Debug: Show event count
        Text(
            text = "${events.size} events",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)
        ) {
            // Time column (fixed width)
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .verticalScroll(scrollState)
            ) {
                // 24 hour slots
                for (hour in 0..23) {
                    Box(
                        modifier = Modifier
                            .height(HOUR_HEIGHT)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = String.format("%02d:00", hour),
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 8.dp, top = 0.dp)
                        )
                    }
                }
            }

            // Timeline and events area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Timeline background with hour lines
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (hour in 0..23) {
                        Box(
                            modifier = Modifier
                                .height(HOUR_HEIGHT)
                                .fillMaxWidth()
                        ) {
                            // Hour line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(TimelineBlue.copy(alpha = 0.1f))
                            )
                        }
                    }
                }

                // Current time indicator
                val currentTimeOffset = calculateTimeOffset(currentTime.toLocalTime(), HOUR_HEIGHT, density)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = currentTimeOffset)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(Color.Red)
                        )
                    }
                }

                // Events overlay
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HOUR_HEIGHT * 24) // Full 24 hours height
                        .padding(start = 16.dp, end = 8.dp)
                ) {
                    val availableWidth = maxWidth

                    // Calculate columns for overlapping events
                    val eventColumns = calculateEventColumns(sortedEvents)

                    eventColumns.forEach { (event, columnInfo) ->
                        val topOffset = calculateTimeOffset(event.startTime.toLocalTime(), HOUR_HEIGHT, density)
                        val eventHeight = calculateEventHeight(event, HOUR_HEIGHT, density)
                        val columnWidth = availableWidth / columnInfo.totalColumns
                        val xOffset = columnWidth * columnInfo.column

                        Box(
                            modifier = Modifier
                                .width(columnWidth - 4.dp)
                                .offset(
                                    x = xOffset,
                                    y = topOffset
                                )
                                .padding(end = 4.dp, bottom = 2.dp)
                        ) {
                            TaskCard(
                                event = event,
                                height = eventHeight,
                                isActive = event.isHappeningNow(currentTime),
                                isPast = event.hasEnded(currentTime)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ColumnInfo(val column: Int, val totalColumns: Int)

/**
 * Calculate which column each event should be in.
 * Non-overlapping events always get full width.
 * Overlapping events share width equally.
 */
fun calculateEventColumns(events: List<CalendarEvent>): Map<CalendarEvent, ColumnInfo> {
    if (events.isEmpty()) return emptyMap()

    val sortedEvents = events.sortedBy { it.startTime }
    val result = mutableMapOf<CalendarEvent, ColumnInfo>()

    // First, find all overlap groups (clusters of events that overlap with each other)
    val processed = mutableSetOf<CalendarEvent>()

    sortedEvents.forEach { event ->
        if (event in processed) return@forEach

        // Find all events in this overlap cluster using BFS
        val cluster = mutableListOf<CalendarEvent>()
        val queue = ArrayDeque<CalendarEvent>()
        queue.add(event)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in processed) continue

            processed.add(current)
            cluster.add(current)

            // Find all events that overlap with current
            sortedEvents.forEach { other ->
                if (other !in processed && eventsOverlap(current, other)) {
                    queue.add(other)
                }
            }
        }

        // Now assign columns within this cluster
        if (cluster.size == 1) {
            // Single event, full width
            result[cluster[0]] = ColumnInfo(0, 1)
        } else {
            // Multiple overlapping events - assign columns greedily
            val clusterSorted = cluster.sortedBy { it.startTime }
            val columnEndTimes = mutableListOf<java.time.LocalDateTime>()

            clusterSorted.forEach { evt ->
                var assignedColumn = -1
                for (colIndex in columnEndTimes.indices) {
                    if (!columnEndTimes[colIndex].isAfter(evt.startTime)) {
                        assignedColumn = colIndex
                        columnEndTimes[colIndex] = evt.endTime
                        break
                    }
                }

                if (assignedColumn == -1) {
                    assignedColumn = columnEndTimes.size
                    columnEndTimes.add(evt.endTime)
                }

                // For now, just store the column; we'll fix totalColumns after
                result[evt] = ColumnInfo(assignedColumn, columnEndTimes.size)
            }

            // Update all events in the cluster to have the same totalColumns
            val maxColumns = columnEndTimes.size
            clusterSorted.forEach { evt ->
                val current = result[evt]!!
                result[evt] = ColumnInfo(current.column, maxColumns)
            }
        }
    }

    return result
}

fun eventsOverlap(event1: CalendarEvent, event2: CalendarEvent): Boolean {
    return event1.startTime.isBefore(event2.endTime) && event1.endTime.isAfter(event2.startTime)
}

@Composable
private fun calculateTimeOffset(time: LocalTime, hourHeight: Dp, density: androidx.compose.ui.unit.Density): Dp {
    val hours = time.hour
    val minutes = time.minute
    val totalMinutes = hours * 60 + minutes
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val offsetPx = (totalMinutes / 60f) * hourHeightPx
    return with(density) { offsetPx.toDp() }
}

@Composable
private fun calculateEventHeight(event: CalendarEvent, hourHeight: Dp, density: androidx.compose.ui.unit.Density): Dp {
    val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
    val hourHeightPx = with(density) { hourHeight.toPx() }
    val heightPx = (durationMinutes / 60f) * hourHeightPx
    return with(density) { maxOf(heightPx, hourHeightPx * 0.5f).toDp() } // Minimum height for visibility
}

@Composable
fun TaskCard(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    isActive: Boolean = false,
    isPast: Boolean = false
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val alpha = when {
        isPast -> 0.4f
        isActive -> 1f
        else -> 0.7f
    }
    val borderColor = when {
        isActive -> TimelineBlue
        else -> TimelineBlue.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TaskCardBackground.copy(alpha = alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            borderColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 20f
                    )
                )
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    color = TextPrimary.copy(alpha = if (isPast) 0.6f else 1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}",
                    color = TextSecondary.copy(alpha = if (isPast) 0.5f else 0.8f),
                    fontSize = 12.sp
                )
//                if (height > 60.dp) {
//                    Spacer(modifier = Modifier.height(2.dp))
//                    Text(
//                        text = event.duration,
//                        color = TextSecondary.copy(alpha = if (isPast) 0.4f else 0.6f),
//                        fontSize = 11.sp
//                    )
//                }
            }

//            Icon(
//                imageVector = Icons.Default.KeyboardArrowDown,
//                contentDescription = "Expand",
//                tint = TextSecondary.copy(alpha = if (isPast) 0.4f else 0.7f),
//                modifier = Modifier.size(20.dp)
//            )
        }
    }
}

