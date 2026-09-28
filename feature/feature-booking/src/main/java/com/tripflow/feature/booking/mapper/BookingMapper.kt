package com.tripflow.feature.booking.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import com.tripflow.core.ui.format.Formatters
import com.tripflow.feature.booking.model.PrenotazioneAttivitaResponse
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.model.StatoPrenotazione
import com.tripflow.feature.booking.ui.ActivityUi
import com.tripflow.feature.booking.ui.BookingUi
import java.math.RoundingMode
import java.time.LocalDate

fun PrenotazioneResponse.toUi(): BookingUi {
    val (action, actionIcon) = when (stato) {
        StatoPrenotazione.IN_ATTESA -> "Completa il pagamento" to Icons.Default.CreditCard
        StatoPrenotazione.COMPLETATA -> "Scrivi una recensione" to Icons.Default.Star
        else -> null to null
    }
    return BookingUi(
        id = id,
        title = titoloViaggio ?: "Viaggio",
        date = Formatters.dateRange(parseDate(dataInizio), parseDate(dataFine)),
        location = destinazione ?: "—",
        participants = numeroPartecipanti,
        price = prezzoTotale.setScale(0, RoundingMode.HALF_UP).toInt(),
        status = stato.name,
        action = action,
        actionIcon = actionIcon
    )
}

fun List<PrenotazioneResponse>.toUi(): List<BookingUi> = map { it.toUi() }

fun PrenotazioneAttivitaResponse.toUi(): ActivityUi = ActivityUi(
    name = nome,
    price = prezzo.setScale(0, RoundingMode.HALF_UP).toInt(),
    isSelected = true,
    duration = Formatters.duration(durataMinuti)
)

private fun parseDate(value: String?): LocalDate? =
    value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
