package com.tripflow.feature.itinerary.ui.addstage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.StageSource
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddStopUiState(
    val itineraryId: String = "",
    val isLoadingContext: Boolean = true,
    val contextError: String? = null,
    val contextErrorRetryable: Boolean = true,
    val itineraryStartDate: LocalDate? = null,
    val itineraryEndDate: LocalDate? = null,
    val stopType: StopType = StopType.CUSTOM,
    val date: LocalDate? = null,
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val customTitle: String = "",
    val customDescription: String = "",
    val notes: String = "",
    val orderIndex: Int = 1,
    val catalogQuery: String = "",
    val catalogResults: List<CatalogItem> = emptyList(),
    val selectedCatalogItem: CatalogItem? = null,
    val isSearchingCatalog: Boolean = false,
    val isSubmitting: Boolean = false,
    val dateError: String? = null,
    val timeError: String? = null,
    val customTitleError: String? = null,
    val catalogError: String? = null,
    val errorMessage: String? = null,
    val addedStageId: String? = null,
)

@HiltViewModel
class AddStopViewModel @Inject constructor(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStopUiState())
    val uiState = _uiState.asStateFlow()

    fun initialize(itineraryId: String) {
        val current = _uiState.value
        if (current.itineraryId == itineraryId && !current.isLoadingContext && current.contextError == null) {
            return
        }

        _uiState.update {
            it.copy(
                itineraryId = itineraryId,
                isLoadingContext = true,
                contextError = null,
                contextErrorRetryable = true,
            )
        }

        viewModelScope.launch {
            when (val result = repository.getItineraryDetail(itineraryId)) {
                is UiState.Success -> {
                    val detail = result.data
                    val nextOrder = (detail.stages.maxOfOrNull { it.orderIndex } ?: 0) + 1
                    _uiState.update {
                        it.copy(
                            isLoadingContext = false,
                            contextError = null,
                            contextErrorRetryable = true,
                            itineraryStartDate = detail.startDate,
                            itineraryEndDate = detail.endDate,
                            date = it.date ?: detail.startDate,
                            orderIndex = nextOrder,
                        )
                    }
                }

                is UiState.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingContext = false,
                            contextError = result.message,
                            contextErrorRetryable = result.retryable,
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoadingContext = false,
                            contextError = "Impossibile caricare l'itinerario",
                        )
                    }
                }
            }
        }
    }

    fun onStopTypeChange(stopType: StopType) {
        _uiState.update {
            it.copy(
                stopType = stopType,
                selectedCatalogItem = null,
                catalogResults = emptyList(),
                catalogError = null,
                errorMessage = null,
            )
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(
                date = date,
                dateError = null,
                errorMessage = null,
            )
        }
    }

    fun onStartTimeChange(value: String) {
        _uiState.update { it.copy(startTime = value, timeError = null, errorMessage = null) }
    }

    fun onEndTimeChange(value: String) {
        _uiState.update { it.copy(endTime = value, timeError = null, errorMessage = null) }
    }

    fun onCustomTitleChange(value: String) {
        if (value.length > CUSTOM_TITLE_MAX_LENGTH) return
        _uiState.update {
            it.copy(customTitle = value, customTitleError = null, errorMessage = null)
        }
    }

    fun onCustomDescriptionChange(value: String) {
        if (value.length > CUSTOM_DESCRIPTION_MAX_LENGTH) return
        _uiState.update { it.copy(customDescription = value, errorMessage = null) }
    }

    fun onNotesChange(value: String) {
        if (value.length > NOTES_MAX_LENGTH) return
        _uiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun onOrderIndexChange(value: Int) {
        if (value < 1) return
        _uiState.update { it.copy(orderIndex = value, errorMessage = null) }
    }

    fun onCatalogQueryChange(value: String) {
        _uiState.update {
            it.copy(
                catalogQuery = value,
                selectedCatalogItem = null,
                catalogError = null,
                errorMessage = null,
            )
        }
    }

    fun searchCatalog() {
        val current = _uiState.value
        if (current.stopType == StopType.CUSTOM || current.isSearchingCatalog) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearchingCatalog = true,
                    catalogError = null,
                    catalogResults = emptyList(),
                )
            }

            when (current.stopType) {
                StopType.TRIP -> {
                    when (
                        val result = repository.searchCatalog(
                            CatalogSearchRequest(query = current.catalogQuery.trim().ifBlank { null })
                        )
                    ) {
                        is UiState.Success -> completeCatalogSearch(result.data.content)
                        is UiState.Error -> failCatalogSearch(result.message)
                        else -> failCatalogSearch("Ricerca non disponibile")
                    }
                }

                StopType.ACTIVITY -> {
                    when (val result = repository.searchActivities(current.catalogQuery.trim())) {
                        is UiState.Success -> completeCatalogSearch(result.data)
                        is UiState.Error -> failCatalogSearch(result.message)
                        else -> failCatalogSearch("Ricerca non disponibile")
                    }
                }

                StopType.CUSTOM -> Unit
            }
        }
    }

    private fun completeCatalogSearch(items: List<CatalogItem>) {
        _uiState.update {
            it.copy(
                isSearchingCatalog = false,
                catalogResults = items,
                catalogError = if (items.isEmpty()) "Nessun risultato trovato" else null,
            )
        }
    }

    private fun failCatalogSearch(message: String) {
        _uiState.update {
            it.copy(
                isSearchingCatalog = false,
                catalogError = message,
            )
        }
    }

    fun selectCatalogItem(item: CatalogItem) {
        _uiState.update {
            it.copy(
                selectedCatalogItem = item,
                catalogError = null,
                errorMessage = null,
            )
        }
    }

    fun addStop() {
        val current = _uiState.value
        if (current.isSubmitting || current.isLoadingContext) return

        val dateError = if (current.date == null) "Seleziona la data della tappa" else null
        val timeError = validateTimes(current.startTime, current.endTime)
        val customTitleError = if (
            current.stopType == StopType.CUSTOM && current.customTitle.isBlank()
        ) {
            "Inserisci un titolo per la tappa"
        } else {
            null
        }
        val catalogError = if (
            current.stopType != StopType.CUSTOM && current.selectedCatalogItem == null
        ) {
            "Seleziona un elemento dal catalogo"
        } else {
            null
        }

        if (dateError != null || timeError != null || customTitleError != null || catalogError != null) {
            _uiState.update {
                it.copy(
                    dateError = dateError,
                    timeError = timeError,
                    customTitleError = customTitleError,
                    catalogError = catalogError,
                )
            }
            return
        }

        val date = requireNotNull(current.date)
        val source = when (current.stopType) {
            StopType.CUSTOM -> StageSource.Custom(
                title = current.customTitle.trim(),
                description = current.customDescription.trim().ifBlank { null },
                location = null,
            )

            StopType.TRIP,
            StopType.ACTIVITY -> StageSource.FromCatalog(
                catalogItemId = requireNotNull(current.selectedCatalogItem).id,
                stopType = current.stopType,
            )
        }
        val dayNumber = current.itineraryStartDate
            ?.let { ChronoUnit.DAYS.between(it, date).toInt() + 1 }
            ?.coerceAtLeast(1)
            ?: 1

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            val request = CreateStageRequest(
                dayNumber = dayNumber,
                date = date,
                startTime = current.startTime,
                endTime = current.endTime,
                source = source,
                notes = current.notes.trim().ifBlank { null },
                orderIndex = current.orderIndex,
            )

            when (val result = repository.addStage(current.itineraryId, request)) {
                is UiState.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            addedStageId = result.data.id,
                        )
                    }
                }

                is UiState.Error -> {
                    if (isOrderConflict(result.message)) {
                        val nextOrder = current.orderIndex + 1
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                orderIndex = nextOrder,
                                errorMessage = "La posizione ${current.orderIndex} è già occupata. " +
                                    "Ho proposto la posizione $nextOrder: riprova.",
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Impossibile aggiungere la tappa",
                        )
                    }
                }
            }
        }
    }

    private fun validateTimes(startTime: String, endTime: String): String? = when {
        !TIME_PATTERN.matches(startTime) -> "Inserisci un orario di inizio valido (HH:mm)"
        !TIME_PATTERN.matches(endTime) -> "Inserisci un orario di fine valido (HH:mm)"
        endTime <= startTime -> "L'orario di fine deve essere successivo a quello di inizio"
        else -> null
    }

    private fun isOrderConflict(message: String): Boolean {
        val normalized = message.lowercase(Locale.ITALY)
        return "conflitt" in normalized ||
            "stop_order" in normalized ||
            "duplicat" in normalized ||
            ("esiste" in normalized && "già" in normalized)
    }

    private companion object {
        const val CUSTOM_TITLE_MAX_LENGTH = 150
        const val CUSTOM_DESCRIPTION_MAX_LENGTH = 150
        const val NOTES_MAX_LENGTH = 1000
        val TIME_PATTERN = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
    }
}
