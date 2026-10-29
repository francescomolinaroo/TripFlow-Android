package com.tripflow.feature.itinerary.ui.itinerarydetail

import com.tripflow.feature.itinerary.model.ItineraryDetail
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Riepilogo testuale dell'itinerario da condividere (WhatsApp, email, note...):
// leggibile ovunque, senza app ne' login. Esempio:
//
//   🗺️ Firenze e il Chianti
//   📆 8 – 10 ottobre 2027 · 3 tappe
//
//   Giorno 1 · ven 8 ottobre
//   • 09:30–12:30  Galleria degli Uffizi
//      📍 Firenze
//      📝 Portare i biglietti
//
//   Organizzato con TripFlow
object ItineraryShareText {

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE d MMMM", Locale.ITALIAN)
    private val fullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)

    fun build(itinerary: ItineraryDetail): String = buildString {
        appendLine("🗺️ ${itinerary.title}")
        val stagesLabel = if (itinerary.stages.size == 1) "1 tappa" else "${itinerary.stages.size} tappe"
        appendLine("📆 ${dateRange(itinerary.startDate, itinerary.endDate)} · $stagesLabel")
        itinerary.description?.takeIf { it.isNotBlank() }?.let { appendLine(it.trim()) }

        groupStagesByDay(itinerary.stages, itinerary.startDate).forEach { day ->
            appendLine()
            appendLine("Giorno ${day.dayNumber} · ${day.date.format(dayFormatter)}")
            day.stages.forEach { stage ->
                val times = listOf(stage.startTime, stage.endTime).filter { it.isNotBlank() }.joinToString("–")
                appendLine(if (times.isBlank()) "• ${stage.title}" else "• $times  ${stage.title}")
                stage.location?.takeIf { it.isNotBlank() }?.let { appendLine("   📍 ${it.trim()}") }
                stage.notes?.takeIf { it.isNotBlank() }?.let { appendLine("   📝 ${it.trim()}") }
            }
        }

        appendLine()
        append("Organizzato con TripFlow")
    }

    // "8 – 10 ottobre 2027", "28 settembre – 3 ottobre 2027", "30 dicembre 2027 – 2 gennaio 2028"
    private fun dateRange(start: LocalDate, end: LocalDate): String = when {
        start == end -> start.format(fullFormatter)
        start.year == end.year && start.month == end.month ->
            "${start.dayOfMonth} – ${end.format(fullFormatter)}"
        start.year == end.year -> "${start.format(dayMonthFormatter)} – ${end.format(fullFormatter)}"
        else -> "${start.format(fullFormatter)} – ${end.format(fullFormatter)}"
    }
}
