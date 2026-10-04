package com.tripflow.feature.review.mapper

import com.tripflow.core.ui.format.Formatters
import com.tripflow.feature.review.model.RecensioneRequest
import com.tripflow.feature.review.model.RecensioneResponse
import com.tripflow.feature.review.model.RecensioneUpdateRequest
import com.tripflow.feature.review.model.TipoOggetto
import com.tripflow.feature.review.ui.WriteReviewUiState
import com.tripflow.feature.review.ui.components.ReviewSummaryUi
import com.tripflow.feature.review.ui.components.ReviewUi
import java.time.Duration
import java.time.LocalDateTime

fun RecensioneResponse.toUi(conOggetto: Boolean = false): ReviewUi {
    val nome: String

    if (autoreNome != null && autoreNome.isNotBlank()) {
        nome = autoreNome
    } else {
        nome = "Viaggiatore"
    }

    val oggetto: String?

    if (conOggetto) {
        oggetto = oggettoNome
    } else {
        oggetto = null
    }

    return ReviewUi(
        name = nome,
        rating = valutazione.toDouble(),
        date = Formatters.date(parseDateTime(createdAt)?.toLocalDate()),
        title = titolo.orEmpty(),
        comment = commento.orEmpty(),
        isModified = isModified(),
        subject = oggetto
    )
}

fun List<RecensioneResponse>.toUi(conOggetto: Boolean = false): List<ReviewUi> = map { it.toUi(conOggetto) }

fun List<RecensioneResponse>.toSummaryUi(): ReviewSummaryUi {

    val average: Double

    if (isEmpty()) {
        average = 0.0
    } else {
        average = map { it.valutazione }.average()
    }

    val distribution = (5 downTo 1).associateWith { stelle ->
        count {
            it.valutazione == stelle
        }
    }

    return ReviewSummaryUi(
        average = average,
        count = size,
        distribution = distribution
    )
}

fun WriteReviewUiState.toRequest(
    prenotazioneId: String,
    oggettoId: String,
    tipoOggetto: TipoOggetto
): RecensioneRequest = RecensioneRequest(
    prenotazioneId = prenotazioneId,
    oggettoId = oggettoId,
    tipoOggetto = tipoOggetto,
    valutazione = rating,
    titolo = title.trim().ifBlank { null },
    commento = comment.trim().ifBlank { null }
)

fun WriteReviewUiState.toUpdateRequest(): RecensioneUpdateRequest = RecensioneUpdateRequest(
    valutazione = rating,
    titolo = title.trim().ifBlank { null },
    commento = comment.trim().ifBlank { null }
)

private fun RecensioneResponse.isModified(): Boolean {
    val creata = parseDateTime(createdAt) ?: return false
    val modificata = parseDateTime(updatedAt) ?: return false
    return Duration.between(creata, modificata).seconds > 1
}

private fun parseDateTime(value: String?): LocalDateTime? =
    value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
