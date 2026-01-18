package com.jamal.desktopclock.data

import java.time.LocalDateTime

data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val duration: String,
    val isAllDay: Boolean = false
) {
    fun isHappeningNow(currentTime: LocalDateTime): Boolean {
        return currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
    }

    fun hasEnded(currentTime: LocalDateTime): Boolean {
        return currentTime.isAfter(endTime)
    }

    fun overlapsWithTime(start: LocalDateTime, end: LocalDateTime): Boolean {
        return !(endTime.isBefore(start) || startTime.isAfter(end))
    }
}

fun formatDuration(startTime: LocalDateTime, endTime: LocalDateTime): String {
    val duration = java.time.Duration.between(startTime, endTime)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    return when {
        hours > 0 && minutes > 0 -> "$hours hours $minutes minutes"
        hours > 0 -> "$hours hours"
        else -> "$minutes minutes"
    }
}
