package com.tripflow.feature.itinerary.ui.createitinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CreateItineraryRequest
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CreateItineraryUiState(
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val visibility: Visibility = Visibility.PRIVATE,
    val titleError: String? = null,
    val dateError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdItineraryId: String? = null,
)

@HiltViewModel
class CreateItineraryViewModel @Inject constructor(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateItineraryUiState())
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        if (value.length > TITLE_MAX_LENGTH) return
        _uiState.update { it.copy(title = value, titleError = null, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        if (value.length > DESCRIPTION_MAX_LENGTH) return
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(
                startDate = date,
                dateError = validateDates(date, it.endDate),
                errorMessage = null,
            )
        }
    }

    fun onEndDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(
                endDate = date,
                dateError = validateDates(it.startDate, date),
                errorMessage = null,
            )
        }
    }

    fun onVisibilityChange(visibility: Visibility) {
        _uiState.update { it.copy(visibility = visibility, errorMessage = null) }
    }

    fun createItinerary() {
        val current = _uiState.value
        if (current.isSubmitting) return

        val titleError = when {
            current.title.isBlank() -> "Inserisci un titolo per l'itinerario"
            current.title.length > TITLE_MAX_LENGTH -> "Il titolo non può superare $TITLE_MAX_LENGTH caratteri"
            else -> null
        }
        val dateError = when {
            current.startDate == null -> "Seleziona la data di inizio"
            current.endDate == null -> "Seleziona la data di fine"
            current.endDate.isBefore(current.startDate) -> "La data di fine non può precedere quella di inizio"
            else -> null
        }

        if (titleError != null || dateError != null) {
            _uiState.update { it.copy(titleError = titleError, dateError = dateError) }
            return
        }

        val startDate = requireNotNull(current.startDate)
        val endDate = requireNotNull(current.endDate)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (
                val result = repository.createItinerary(
                    CreateItineraryRequest(
                        title = current.title.trim(),
                        description = current.description.trim().ifBlank { null },
                        startDate = startDate.toString(),
                        endDate = endDate.toString(),
                        visibility = current.visibility,
                    )
                )
            ) {
                is UiState.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdItineraryId = result.data.id.toString(),
                        )
                    }
                }

                is UiState.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message,
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Impossibile creare l'itinerario",
                        )
                    }
                }
            }
        }
    }

    fun startNewCreation() {
        _uiState.value = CreateItineraryUiState()
    }

    private fun validateDates(startDate: LocalDate?, endDate: LocalDate?): String? =
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            "La data di fine non può precedere quella di inizio"
        } else {
            null
        }

    private companion object {
        const val TITLE_MAX_LENGTH = 150
        const val DESCRIPTION_MAX_LENGTH = 1000
    }
}