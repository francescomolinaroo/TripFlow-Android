package com.tripflow.feature.booking.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import com.tripflow.core.network.catalog.dto.ActivityResponseDTO
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.core.ui.format.Formatters
import com.tripflow.feature.booking.model.PagamentoResponse
import com.tripflow.feature.booking.model.PrenotazioneAttivitaResponse
import com.tripflow.feature.booking.model.PrenotazioneResponse
import com.tripflow.feature.booking.model.StatoPagamento
import com.tripflow.feature.booking.model.StatoPrenotazione
import com.tripflow.feature.booking.ui.ActivityUi
import com.tripflow.feature.booking.ui.BookingDetailUi
import com.tripflow.feature.booking.ui.BookingUi
import com.tripflow.feature.booking.ui.ReceivedBookingUi
import com.tripflow.feature.booking.ui.TripSummaryUi
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime

fun PrenotazioneResponse.toUi(recensita: Boolean = false): BookingUi {
    val (action, actionIcon) = when (stato) {
        StatoPrenotazione.IN_ATTESA -> "Completa il pagamento" to Icons.Default.CreditCard
        StatoPrenotazione.COMPLETATA -> if (recensita) {
            "Recensione inviata" to Icons.Default.CheckCircle
        } else {
            "Scrivi una recensione" to Icons.Default.Star
        }
        else -> null to null
    }
    return BookingUi(
        id = id,
        tripId = viaggioId,
        title = titoloViaggio ?: "Viaggio",
        date = Formatters.dateRange(parseDate(dataInizio), parseDate(dataFine)),
        location = destinazione ?: "—",
        imageUrl = immagineViaggio,
        participants = numeroPartecipanti,
        price = prezzoTotale.setScale(0, RoundingMode.HALF_UP).toInt(),
        status = stato.name,
        reviewed = recensita,
        action = action,
        actionIcon = actionIcon
    )
}

fun List<PrenotazioneResponse>.toUi(recensite: Set<String> = emptySet()): List<BookingUi> =
    map { it.toUi(recensita = it.id in recensite) }

fun PrenotazioneResponse.toReceivedUi(): ReceivedBookingUi = ReceivedBookingUi(
    id = id,
    tripId = viaggioId,
    travelerName = nomeViaggiatore(),
    tripTitle = titoloViaggio ?: "Viaggio",
    tripDate = Formatters.dateRange(parseDate(dataInizio), parseDate(dataFine)),
    participants = Formatters.partecipanti(numeroPartecipanti),
    totalPrice = Formatters.money(prezzoTotale),
    status = stato.name,
    bookedAt = Formatters.dateTime(parseDateTime(dataPrenotazione))
)

private fun PrenotazioneResponse.nomeViaggiatore(): String {
    val nome = viaggiatoreNome
    if (nome == null || nome.isBlank()) {
        return "Viaggiatore"
    }
    return nome
}

fun PrenotazioneResponse.toDetailUi(vistaOrganizzatore: Boolean = false): BookingDetailUi {
    val pagamento = infoPagamento
    var canPay = stato == StatoPrenotazione.IN_ATTESA &&
        (pagamento == null || pagamento.stato == StatoPagamento.FALLITO || pagamento.stato == StatoPagamento.IN_ATTESA)
    var canCancel = stato == StatoPrenotazione.IN_ATTESA || stato == StatoPrenotazione.CONFERMATA
    var travelerName: String? = null
    if (vistaOrganizzatore) {
        canPay = false
        canCancel = false
        travelerName = nomeViaggiatore()
    }
    var totalPrice = Formatters.money(prezzoTotale)
    var discountCode: String? = null
    var subtotalPrice: String? = null
    var discountAmount: String? = null
    val importoOriginale = pagamento?.importoOriginale
    if (pagamento != null && pagamento.codiceSconto != null && importoOriginale != null
        && importoOriginale.compareTo(prezzoTotale) == 0
    ) {
        discountCode = pagamento.codiceSconto
        subtotalPrice = Formatters.money(importoOriginale)
        discountAmount = "− " + Formatters.money(importoOriginale.subtract(pagamento.importo))
        totalPrice = Formatters.money(pagamento.importo)
    }
    return BookingDetailUi(
        id = id,
        trip = TripSummaryUi(
            title = titoloViaggio ?: "Viaggio",
            date = Formatters.dateRange(parseDate(dataInizio), parseDate(dataFine)),
            location = destinazione ?: "—",
            imageUrl = immagineViaggio
        ),
        status = stato.name,
        participants = numeroPartecipanti,
        unitPrice = Formatters.money(prezzoUnitarioAlMomentoDelBooking),
        totalPrice = totalPrice,
        activities = attivitaSelezionate.orEmpty().map { it.toUi() },
        notes = note?.takeIf { it.isNotBlank() },
        paymentStatus = pagamento?.stato?.name,
        paymentDetail = pagamento?.toDetail(),
        canPay = canPay,
        canCancel = canCancel,
        travelerName = travelerName,
        discountCode = discountCode,
        subtotalPrice = subtotalPrice,
        discountAmount = discountAmount
    )
}

private fun PagamentoResponse.toDetail(): String = when (stato) {
    StatoPagamento.COMPLETATO -> listOfNotNull(
        brandCarta?.replaceFirstChar { it.uppercase() },
        ultimeQuattroCifre?.let { "•$it" },
        parseDateTime(dataPagamento)?.let { Formatters.dateTime(it) }
    ).joinToString(" · ").ifBlank { "Pagamento completato" }
    StatoPagamento.IN_ATTESA -> "In attesa di conferma"
    StatoPagamento.FALLITO -> "Pagamento non riuscito, puoi riprovare"
    StatoPagamento.RIMBORSATO -> "Importo rimborsato"
}

private fun parseDateTime(value: String?): LocalDateTime? =
    value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }

fun PrenotazioneAttivitaResponse.toUi(): ActivityUi = ActivityUi(
    id = attivitaId,
    name = nome,
    price = prezzo.setScale(0, RoundingMode.HALF_UP).toInt(),
    isSelected = true,
    duration = Formatters.duration(durataMinuti)
)


fun TripResponseDTO.toSummaryUi(): TripSummaryUi = TripSummaryUi(
    title = name,
    date = Formatters.dateRange(parseDate(startDate), parseDate(endDate)),
    location = destination,
    imageUrl = images?.firstOrNull()
)


fun ActivityResponseDTO.toUi(): ActivityUi = ActivityUi(
    id = id,
    name = name,
    price = BigDecimal.valueOf(price).setScale(0, RoundingMode.HALF_UP).toInt(),
    isSelected = false,
    duration = Formatters.duration(duration),
    availableSpots = availableSpots
)


private fun parseDate(value: String?): LocalDate? =
    value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
