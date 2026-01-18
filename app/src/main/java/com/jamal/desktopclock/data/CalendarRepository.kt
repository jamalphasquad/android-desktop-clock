package com.jamal.desktopclock.data

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CalendarRepository(private val contentResolver: ContentResolver) {

    companion object {
        private const val TAG = "CalendarRepository"

        // Use Instances projection for recurring events support
        private val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )

        private const val PROJECTION_EVENT_ID_INDEX = 0
        private const val PROJECTION_TITLE_INDEX = 1
        private const val PROJECTION_BEGIN_INDEX = 2
        private const val PROJECTION_END_INDEX = 3
        private const val PROJECTION_ALL_DAY_INDEX = 4
    }

    fun getTodayEvents(): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        val now = LocalDateTime.now()
        val startOfDay = now.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)

        val startMillis = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Use Instances URI builder which handles recurring events properly
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)
        val uri = builder.build()

        try {
            val cursor: Cursor? = contentResolver.query(
                uri,
                INSTANCE_PROJECTION,
                null, // No selection needed, time range is in URI
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            Log.d(TAG, "Query returned ${cursor?.count ?: 0} events")

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(PROJECTION_EVENT_ID_INDEX).toString()
                    val title = it.getString(PROJECTION_TITLE_INDEX) ?: "Untitled Event"
                    val startTimeMillis = it.getLong(PROJECTION_BEGIN_INDEX)
                    val endTimeMillis = it.getLong(PROJECTION_END_INDEX)
                    val isAllDay = it.getInt(PROJECTION_ALL_DAY_INDEX) == 1

                    val startTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(startTimeMillis),
                        ZoneId.systemDefault()
                    )
                    val endTime = if (endTimeMillis > 0) {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(endTimeMillis),
                            ZoneId.systemDefault()
                        )
                    } else {
                        startTime.plusHours(1)
                    }

                    Log.d(TAG, "Event: $title at $startTime - $endTime")

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            duration = formatDuration(startTime, endTime),
                            isAllDay = isAllDay
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching events", e)
        }

        Log.d(TAG, "Returning ${events.size} events")
        return events.sortedBy { it.startTime }
    }
}
