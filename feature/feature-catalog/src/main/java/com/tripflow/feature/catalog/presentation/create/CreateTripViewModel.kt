package com.tripflow.feature.catalog.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripflow.core.network.catalog.dto.TripRequestDTO
import com.tripflow.feature.catalog.data.repository.CatalogRepositoryImpl
import com.tripflow.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Form state ──────────────────────────────────────────────────────────────

data class CreateTripFormState(
    val name: String = "",
    val destination: String = "",
    val startDate: String = "",   // "yyyy-MM-dd"
    val endDate: String = "",     // "yyyy-MM-dd"
    val price: String = "",
    val availableSpots: String = "",
    val description: String = "",
    val imagesRaw: String = "",   // URL separati da virgola

    // Errori di campo
    val nameError: String? = null,
    val destinationError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val priceError: String? = null,
    val availableSpotsError: String? = null,

    // Stato submit
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val globalError: String? = null
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class CreateTripViewModel(
    private val repository: CatalogRepository = CatalogRepositoryImpl()
) : ViewModel() {

    private val _form = MutableStateFlow(CreateTripFormState())
    val form: StateFlow<CreateTripFormState> = _form.asStateFlow()

    // Field updates
    fun onNameChange(v: String) = _form.update { it.copy(name = v, nameError = null, globalError = null) }
    fun onDestinationChange(v: String) = _form.update { it.copy(destination = v, destinationError = null, globalError = null) }
    fun onStartDateChange(v: String) = _form.update { it.copy(startDate = v, startDateError = null, endDateError = null, globalError = null) }
    fun onEndDateChange(v: String) = _form.update { it.copy(endDate = v, endDateError = null, globalError = null) }
    fun onPriceChange(v: String) = _form.update { it.copy(price = v, priceError = null, globalError = null) }
    fun onAvailableSpotsChange(v: String) = _form.update { it.copy(availableSpots = v, availableSpotsError = null, globalError = null) }
    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v, globalError = null) }
    fun onImagesRawChange(v: String) = _form.update { it.copy(imagesRaw = v, globalError = null) }

    fun dismissSuccess() = _form.update { it.copy(successMessage = null) }
    fun dismissError() = _form.update { it.copy(globalError = null) }

    fun submit() {
        val s = _form.value
        if (!validate(s)) return

        val images = s.imagesRaw
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { null }

        val request = TripRequestDTO(
            name = s.name.trim(),
            destination = s.destination.trim(),
            startDate = s.startDate.trim(),
            endDate = s.endDate.trim(),
            price = s.price.toDouble(),
            availableSpots = s.availableSpots.toInt(),
            description = s.description.trim().takeIf { it.isNotBlank() },
            images = images
        )

        _form.update { it.copy(isLoading = true, globalError = null) }

        viewModelScope.launch {
            repository.createTrip(request).fold(
                onSuccess = {
                    _form.update { state ->
                        CreateTripFormState(
                            successMessage = "Viaggio \"${it.name}\" creato con successo!"
                        )
                    }
                },
                onFailure = { e ->
                    val msg = when {
                        e.message?.contains("401") == true -> "Sessione scaduta, effettua di nuovo il login"
                        e.message?.contains("403") == true -> "Non hai i permessi per creare un viaggio"
                        e.message?.contains("400") == true -> "Dati non validi: controlla i campi"
                        else -> e.message ?: "Errore di rete, riprova"
                    }
                    _form.update { it.copy(isLoading = false, globalError = msg) }
                }
            )
        }
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private val iso = DateTimeFormatter.ISO_LOCAL_DATE

    private fun validate(s: CreateTripFormState): Boolean {
        var ok = true

        val nameErr = when {
            s.name.isBlank() -> "Il nome è obbligatorio"
            s.name.length < 3 -> "Il nome deve avere almeno 3 caratteri"
            else -> null
        }
        val destErr = if (s.destination.isBlank()) "La destinazione è obbligatoria" else null

        val startDate = runCatching { LocalDate.parse(s.startDate.trim(), iso) }.getOrNull()
        val endDate = runCatching { LocalDate.parse(s.endDate.trim(), iso) }.getOrNull()

        val startErr = when {
            s.startDate.isBlank() -> "La data di inizio è obbligatoria"
            startDate == null -> "Formato non valido (usa aaaa-mm-gg)"
            startDate.isBefore(LocalDate.now()) -> "La data di inizio non può essere nel passato"
            else -> null
        }
        val endErr = when {
            s.endDate.isBlank() -> "La data di fine è obbligatoria"
            endDate == null -> "Formato non valido (usa aaaa-mm-gg)"
            startDate != null && !endDate.isAfter(startDate) -> "La data di fine deve essere dopo quella di inizio"
            else -> null
        }

        val price = s.price.toDoubleOrNull()
        val priceErr = when {
            s.price.isBlank() -> "Il prezzo è obbligatorio"
            price == null -> "Inserisci un numero valido"
            price <= 0 -> "Il prezzo deve essere maggiore di 0"
            else -> null
        }

        val spots = s.availableSpots.toIntOrNull()
        val spotsErr = when {
            s.availableSpots.isBlank() -> "I posti disponibili sono obbligatori"
            spots == null -> "Inserisci un numero intero"
            spots <= 0 -> "Ci deve essere almeno 1 posto disponibile"
            else -> null
        }

        if (listOf(nameErr, destErr, startErr, endErr, priceErr, spotsErr).any { it != null }) ok = false

        _form.update {
            it.copy(
                nameError = nameErr,
                destinationError = destErr,
                startDateError = startErr,
                endDateError = endErr,
                priceError = priceErr,
                availableSpotsError = spotsErr
            )
        }
        return ok
    }
}
