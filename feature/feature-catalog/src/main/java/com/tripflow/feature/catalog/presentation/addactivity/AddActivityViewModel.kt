package com.tripflow.feature.catalog.presentation.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.network.NetworkErrorParser
import com.tripflow.core.network.catalog.dto.ActivityRequestDTO
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Form state ──────────────────────────────────────────────────────────────

data class AddActivityFormState(
    val name: String = "",
    val description: String = "",
    val duration: String = "",        // minuti
    val price: String = "",
    val availableSpots: String = "",

    // Errori di campo
    val nameError: String? = null,
    val durationError: String? = null,
    val priceError: String? = null,
    val availableSpotsError: String? = null,

    // Stato submit
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val globalError: String? = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class AddActivityViewModel(
    private val tripId: String,
    private val repository: CatalogRepository = CatalogRepositoryImpl()
) : ViewModel() {

    private val _form = MutableStateFlow(AddActivityFormState())
    val form: StateFlow<AddActivityFormState> = _form.asStateFlow()

    // Field updates
    fun onNameChange(v: String) = _form.update { it.copy(name = v, nameError = null, globalError = null) }
    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v, globalError = null) }
    fun onDurationChange(v: String) = _form.update { it.copy(duration = v, durationError = null, globalError = null) }
    fun onPriceChange(v: String) = _form.update { it.copy(price = v, priceError = null, globalError = null) }
    fun onAvailableSpotsChange(v: String) = _form.update { it.copy(availableSpots = v, availableSpotsError = null, globalError = null) }

    fun dismissSuccess() = _form.update { it.copy(successMessage = null) }
    fun dismissError() = _form.update { it.copy(globalError = null) }

    fun submit() {
        val s = _form.value
        if (!validate(s)) return

        val request = ActivityRequestDTO(
            name = s.name.trim(),
            description = s.description.trim().takeIf { it.isNotBlank() },
            duration = s.duration.trim().toInt(),
            price = s.price.trim().toDouble(),
            availableSpots = s.availableSpots.trim().toInt(),
            tripId = tripId
        )

        _form.update { it.copy(isLoading = true, globalError = null) }

        viewModelScope.launch {
            repository.createActivity(request).fold(
                onSuccess = { created ->
                    _form.update {
                        AddActivityFormState(
                            successMessage = "Attività \"${created.name}\" aggiunta con successo!"
                        )
                    }
                },
                onFailure = { e ->
                    val msg = NetworkErrorParser.getUserMessage(
                        throwable = e,
                        fallback400 = "Dati non validi: controlla i campi",
                        fallback403 = "Non hai i permessi per aggiungere attività"
                    )
                    _form.update { it.copy(isLoading = false, globalError = msg) }
                }
            )
        }
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private fun validate(s: AddActivityFormState): Boolean {
        var ok = true

        val nameErr = when {
            s.name.isBlank() -> "Il nome è obbligatorio"
            s.name.length < 3 -> "Il nome deve avere almeno 3 caratteri"
            else -> null
        }

        val duration = s.duration.trim().toIntOrNull()
        val durationErr = when {
            s.duration.isBlank() -> "La durata è obbligatoria"
            duration == null -> "Inserisci un numero intero"
            duration <= 0 -> "La durata deve essere maggiore di 0"
            else -> null
        }

        val price = s.price.trim().toDoubleOrNull()
        val priceErr = when {
            s.price.isBlank() -> "Il prezzo è obbligatorio"
            price == null -> "Inserisci un numero valido"
            price < 0 -> "Il prezzo non può essere negativo"
            else -> null
        }

        val spots = s.availableSpots.trim().toIntOrNull()
        val spotsErr = when {
            s.availableSpots.isBlank() -> "I posti disponibili sono obbligatori"
            spots == null -> "Inserisci un numero intero"
            spots <= 0 -> "Ci deve essere almeno 1 posto disponibile"
            else -> null
        }

        if (listOf(nameErr, durationErr, priceErr, spotsErr).any { it != null }) ok = false

        _form.update {
            it.copy(
                nameError = nameErr,
                durationError = durationErr,
                priceError = priceErr,
                availableSpotsError = spotsErr
            )
        }
        return ok
    }
}

