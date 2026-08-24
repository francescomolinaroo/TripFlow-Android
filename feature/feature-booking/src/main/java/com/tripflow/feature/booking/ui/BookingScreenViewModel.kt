package com.tripflow.feature.booking.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BookingScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        BookingScreenUiState(
            activities = listOf(
                ActivityUi("Tour in barca a Capri", 45, true, "4h"),
                ActivityUi("Degustazione di limoncello", 30, false, "1h 30m"),
                ActivityUi("Sentiero degli Dei", 25, false, "5h")
            )
        )
    )
    val uiState: StateFlow<BookingScreenUiState> = _uiState.asStateFlow()

    fun onParticipantsChange(count: Int) {
        _uiState.update { it.copy(participants = count) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onToggleActivity(activityName: String) {
        _uiState.update { state ->
            val updatedActivities = state.activities.map { activity ->
                if (activity.name == activityName) {
                    activity.copy(isSelected = !activity.isSelected)
                } else {
                    activity
                }
            }
            state.copy(activities = updatedActivities)
        }
    }
}
