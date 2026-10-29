package com.tripflow.feature.itinerary.ui.itinerarydetail

import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StopType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class TimelineAndCalendarTest {

    private val start = LocalDate.of(2030, 10, 8)
    private val rome = ZoneId.of("Europe/Rome")

    private fun stage(id: String, date: LocalDate, from: String, to: String, order: Int = 0) = StageDetail(
        id = id, dayNumber = 0, date = date, startTime = from, endTime = to,
        title = "Tappa $id", description = null, location = "Firenze", stopType = StopType.CUSTOM,
        viaggioId = null, attivitaId = null, notes = "Portare i biglietti", orderIndex = order,
    )

    @Test
    fun `stages are grouped by day and sorted by time`() {
        val days = groupStagesByDay(
            listOf(
                stage("c", start.plusDays(1), "10:00", "12:00"),
                stage("b", start, "13:00", "14:30"),
                stage("a", start, "09:30", "12:30"),
            ),
            itineraryStart = start,
        )

        assertEquals(listOf(1, 2), days.map { it.dayNumber })
        assertEquals(listOf("a", "b"), days[0].stages.map { it.id })
        assertEquals(listOf("c"), days[1].stages.map { it.id })
    }

    @Test
    fun `calendar event uses stage times title and notes`() {
        val event = CalendarEvent.from(stage("a", start, "09:30", "12:30"), "Firenze e il Chianti", rome)

        assertEquals(ZonedDateTime.of(2030, 10, 8, 9, 30, 0, 0, rome).toInstant().toEpochMilli(), event.beginMillis)
        assertEquals(ZonedDateTime.of(2030, 10, 8, 12, 30, 0, 0, rome).toInstant().toEpochMilli(), event.endMillis)
        assertEquals("Tappa a", event.title)
        assertEquals("Firenze", event.location)
        assertTrue(event.description.contains("Portare i biglietti"))
        assertTrue(event.description.contains("Firenze e il Chianti"))
        assertFalse(event.allDay)
    }

    @Test
    fun `missing or invalid end time lasts one hour and missing start is all day`() {
        val noEnd = CalendarEvent.from(stage("a", start, "18:00", ""), "Viaggio", rome)
        assertEquals(60 * 60 * 1000L, noEnd.endMillis - noEnd.beginMillis)

        val noStart = CalendarEvent.from(stage("b", start, "", ""), "Viaggio", rome)
        assertTrue(noStart.allDay)
    }
}
