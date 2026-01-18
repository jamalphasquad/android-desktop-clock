package com.jamal.desktopclock.viewmodel

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jamal.desktopclock.data.CalendarEvent
import com.jamal.desktopclock.data.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(contentResolver: ContentResolver) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val calendarRepository = CalendarRepository(contentResolver)

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasCalendarPermission = MutableStateFlow(false)
    val hasCalendarPermission: StateFlow<Boolean> = _hasCalendarPermission.asStateFlow()

    fun setCalendarPermission(granted: Boolean) {
        Log.d(TAG, "Calendar permission granted: $granted")
        _hasCalendarPermission.value = granted
        if (granted) {
            loadTodayEvents()
        }
    }

    fun loadTodayEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val todayEvents = withContext(Dispatchers.IO) {
                    calendarRepository.getTodayEvents()
                }
                Log.d(TAG, "Loaded ${todayEvents.size} events")
                todayEvents.forEach { event ->
//                    Log.d(TAG, "Event: ${event.title} at ${event.startTime} - ${event.endTime}")
                }
                _events.value = todayEvents
            } catch (e: Exception) {
                Log.e(TAG, "Error loading events", e)
                _events.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshEvents() {
        Log.d(TAG, "Refreshing events, permission: ${_hasCalendarPermission.value}")
        if (_hasCalendarPermission.value) {
            loadTodayEvents()
        }
    }
}

class MainViewModelFactory(private val contentResolver: ContentResolver) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(contentResolver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
