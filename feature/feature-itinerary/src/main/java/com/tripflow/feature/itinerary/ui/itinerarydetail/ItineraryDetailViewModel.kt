package com.tripflow.feature.itinerary.ui.itinerarydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItineraryShare
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import com.tripflow.feature.itinerary.session.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItineraryDetailActionState(
    val deletingStageId: String? = null,
    val isUpdatingVisibility: Boolean = false,
    val isDeletingItinerary: Boolean = false,
    // true dopo l'eliminazione: la schermata torna indietro.
    val itineraryDeleted: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
)

// Stato della finestra "Condividi con un utente".
data class ShareUiState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val shares: List<ItineraryShare> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ItineraryDetailViewModel @Inject constructor(
    private val repository: ItineraryRepository,
    private val currentUserProvider: CurrentUserProvider = CurrentUserProvider.None,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ItineraryDetail>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow(ItineraryDetailActionState())
    val actionState = _actionState.asStateFlow()

    private val _shareState = MutableStateFlow(ShareUiState())
    val shareState = _shareState.asStateFlow()

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

    // Solo il proprietario puo' cambiare la visibilita' (il backend risponde 403 agli altri,
    // anche agli editor con canEdit = true).
    // Letto una sola volta: il ViewModel vive nella sessione corrente (al logout viene ricreato).
    private val currentUserId: String? by lazy { currentUserProvider.currentUserId() }

    fun isOwner(detail: ItineraryDetail): Boolean {
        val userId = currentUserId ?: return false
        return detail.ownerId != null && detail.ownerId.equals(userId, ignoreCase = true)
    }

    fun updateVisibility(visibility: Visibility) {
        val id = itineraryId ?: return
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (_actionState.value.isUpdatingVisibility || current.visibility == visibility) return

        viewModelScope.launch {
            _actionState.update { it.copy(isUpdatingVisibility = true, message = null, errorMessage = null) }
            when (val result = repository.updateVisibility(id, visibility)) {
                is UiState.Success -> {
                    _uiState.value = UiState.Success(result.data.sortedByOrder())
                    _actionState.update {
                        it.copy(isUpdatingVisibility = false, message = "Visibilita' aggiornata")
                    }
                }

                is UiState.Error -> {
                    _actionState.update {
                        it.copy(isUpdatingVisibility = false, errorMessage = result.message)
                    }
                }

                else -> {
                    _actionState.update {
                        it.copy(
                            isUpdatingVisibility = false,
                            errorMessage = "Impossibile aggiornare la visibilita'",
                        )
                    }
                }
            }
        }
    }

    // Solo il proprietario (il backend risponde 403 agli altri).
    fun deleteItinerary() {
        val id = itineraryId ?: return
        if (_actionState.value.isDeletingItinerary) return
        viewModelScope.launch {
            _actionState.update { it.copy(isDeletingItinerary = true, message = null, errorMessage = null) }
            when (val result = repository.deleteItinerary(id)) {
                is UiState.Success -> _actionState.update {
                    it.copy(isDeletingItinerary = false, itineraryDeleted = true)
                }
                is UiState.Error -> _actionState.update {
                    it.copy(isDeletingItinerary = false, errorMessage = result.message)
                }
                else -> _actionState.update {
                    it.copy(isDeletingItinerary = false, errorMessage = "Impossibile eliminare l'itinerario")
                }
            }
        }
    }

    // "Condiviso" scelto da "Cambia visibilita'": da PRIVATE basta aggiungere persone
    // (la prima condivisione rende l'itinerario SHARED); da PUBLIC prima si passa a SHARED,
    // che revoca il link pubblico, poi si apre la scelta delle persone.
    fun switchToShared() {
        val id = itineraryId ?: return
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (current.visibility != Visibility.PUBLIC) {
            openShares()
            return
        }
        if (_actionState.value.isUpdatingVisibility) return
        viewModelScope.launch {
            _actionState.update { it.copy(isUpdatingVisibility = true, message = null, errorMessage = null) }
            when (val result = repository.updateVisibility(id, Visibility.SHARED)) {
                is UiState.Success -> {
                    _uiState.value = UiState.Success(result.data.sortedByOrder())
                    _actionState.update { it.copy(isUpdatingVisibility = false) }
                    openShares()
                }
                is UiState.Error -> _actionState.update {
                    it.copy(isUpdatingVisibility = false, errorMessage = result.message)
                }
                else -> _actionState.update {
                    it.copy(isUpdatingVisibility = false, errorMessage = "Impossibile aggiornare la visibilita'")
                }
            }
        }
    }

    fun openShares() {
        val id = itineraryId ?: return
        _shareState.value = ShareUiState(isOpen = true, isLoading = true)
        viewModelScope.launch {
            when (val result = repository.getShares(id)) {
                is UiState.Success -> _shareState.update { it.copy(isLoading = false, shares = result.data) }
                is UiState.Error -> _shareState.update { it.copy(isLoading = false, error = result.message) }
                else -> _shareState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun closeShares() {
        _shareState.value = ShareUiState()
    }

    // Il codice e' l'id utente (UUID) che il destinatario vede in "Il mio codice".
    fun shareWith(code: String, canEdit: Boolean) {
        val id = itineraryId ?: return
        if (_shareState.value.isSubmitting) return
        val userId = code.trim()
        if (runCatching { java.util.UUID.fromString(userId) }.isFailure) {
            _shareState.update { it.copy(error = "Codice utente non valido") }
            return
        }
        viewModelScope.launch {
            _shareState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.shareItinerary(id, userId, canEdit)) {
                is UiState.Success -> {
                    _shareState.update { it.copy(isSubmitting = false, shares = it.shares + result.data) }
                    _actionState.update { it.copy(message = "Itinerario condiviso") }
                    // La prima condivisione rende SHARED un itinerario privato.
                    refreshDetail(id)
                }
                is UiState.Error -> _shareState.update { it.copy(isSubmitting = false, error = result.message) }
                else -> _shareState.update { it.copy(isSubmitting = false, error = "Impossibile condividere") }
            }
        }
    }

    fun removeShare(userId: String) {
        val id = itineraryId ?: return
        viewModelScope.launch {
            when (val result = repository.removeShare(id, userId)) {
                is UiState.Success -> {
                    _shareState.update { state -> state.copy(shares = state.shares.filterNot { it.userId == userId }, error = null) }
                    _actionState.update { it.copy(message = "Condivisione rimossa") }
                    // Rimossa l'ultima condivisione, l'itinerario torna PRIVATE.
                    refreshDetail(id)
                }
                is UiState.Error -> _shareState.update { it.copy(error = result.message) }
                else -> _shareState.update { it.copy(error = "Impossibile rimuovere la condivisione") }
            }
        }
    }

    // Aggiorna il dettaglio senza passare da Loading (niente schermata di caricamento).
    private suspend fun refreshDetail(id: String) {
        val result = repository.getItineraryDetail(id)
        if (result is UiState.Success) _uiState.value = UiState.Success(result.data.sortedByOrder())
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