package com.tripflow.feature.itinerary.ui.itinerarydetail

import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.model.Visibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate

class ItineraryShareTextTest {

    private val start = LocalDate.of(2027, 10, 8)

    private fun stage(id: String, date: LocalDate, from: String, to: String, title: String, notes: String? = null, location: String? = null) =
        StageDetail(
            id = id, dayNumber = 0, date = date, startTime = from, endTime = to, title = title,
            description = null, location = location, stopType = StopType.CUSTOM,
            viaggioId = null, attivitaId = null, notes = notes, orderIndex = 0,
        )

    private fun itinerary(stages: List<StageDetail>) = ItineraryDetail(
        id = "1", ownerId = "owner", title = "Firenze e il Chianti", description = null,
        startDate = start, endDate = start.plusDays(2), visibility = Visibility.PUBLIC, canEdit = true,
        stages = stages, totalStages = stages.size, createdAt = "", updatedAt = "",
    )

    @Test
    fun `builds a readable summary grouped by day`() {
        val text = ItineraryShareText.build(
            itinerary(
                listOf(
                    stage("2", start, "13:00", "14:30", "Pranzo al Mercato Centrale"),
                    stage("1", start, "09:30", "12:30", "Galleria degli Uffizi", notes = "Portare i biglietti", location = "Firenze"),
                    stage("3", start.plusDays(1), "10:00", "12:00", "Degustazione in cantina"),
                )
            )
        )

        val expected = """
            🗺️ Firenze e il Chianti
            📆 8 – 10 ottobre 2027 · 3 tappe

            Giorno 1 · ven 8 ottobre
            • 09:30–12:30  Galleria degli Uffizi
               📍 Firenze
               📝 Portare i biglietti
            • 13:00–14:30  Pranzo al Mercato Centrale

            Giorno 2 · sab 9 ottobre
            • 10:00–12:00  Degustazione in cantina

            Organizzato con TripFlow
        """.trimIndent()
        assertEquals(expected, text)
    }

    @Test
    fun `summary without stages has no day sections and no api link`() {
        val text = ItineraryShareText.build(itinerary(emptyList()))

        assertEquals("🗺️ Firenze e il Chianti\n📆 8 – 10 ottobre 2027 · 0 tappe\n\nOrganizzato con TripFlow", text)
        assertFalse(text.contains("http"))
    }
}
