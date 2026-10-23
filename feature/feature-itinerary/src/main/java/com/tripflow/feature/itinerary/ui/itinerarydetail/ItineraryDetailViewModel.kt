package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItineraryDetailActionState(
    val deletingStageId: String? = null,
    val message: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ItineraryDetailViewModel @Inject constructor(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ItineraryDetail>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(ItineraryDetailActionState())
    val actionState = _actionState.asStateFlow()

    private var itineraryId: String? = null

    fun loadItinerary(id: String) {
        itineraryId = id
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            if (id.isBlank()) {
                _uiState.value = UiState.Error("Identificativo itinerario non valido")
                return@launch
            }
            _uiState.value = when (val result = repository.getItineraryDetail(id)) {
                is UiState.Success -> UiState.Success(result.data.sortedByOrder())
                else -> result
            }
        }
    }

    fun deleteStage(stageId: String) {
        val id = itineraryId ?: return
        if (_actionState.value.deletingStageId != null) return

        viewModelScope.launch {
            _actionState.update {
                it.copy(
                    deletingStageId = stageId,
                    message = null,
                    errorMessage = null,
                )
            }

            if (id.isBlank() || stageId.isBlank()) {
                _actionState.update {
                    it.copy(
                        deletingStageId = null,
                        errorMessage = "Identificativo non valido",
                    )
                }
                return@launch
            }

            when (val result = repository.deleteStage(id, stageId)) {
                is UiState.Success -> {
                    removeStage(stageId)
                    _actionState.update {
                        it.copy(
                            deletingStageId = null,
                            message = "Tappa eliminata",
                        )
                    }
                }

                is UiState.Error -> {
                    _actionState.update {
                        it.copy(
                            deletingStageId = null,
                            errorMessage = result.message,
                        )
                    }
                }

                else -> {
                    _actionState.update {
                        it.copy(
                            deletingStageId = null,
                            errorMessage = "Impossibile eliminare la tappa",
                        )
                    }
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionState.update { it.copy(message = null, errorMessage = null) }
    }

    private fun removeStage(stageId: String) {
        val current = _uiState.value
        if (current !is UiState.Success) return

        val remainingStages = current.data.stages.filterNot { it.id.toString() == stageId }
        _uiState.value = UiState.Success(
            current.data.copy(
                stages = remainingStages,
                totalStages = remainingStages.size,
            )
        )
    }

    private fun ItineraryDetail.sortedByOrder(): ItineraryDetail = copy(
        stages = stages.sortedWith(
            compareBy(
                { it.orderIndex },
                { it.date },
                { it.startTime },
            )
        ),
        totalStages = stages.size,
    )
}