package com.tripflow.feature.itinerary.data.mapper

import com.tripflow.feature.itinerary.data.remote.dto.ItineraryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StopResponseDto
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.StageSource
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.model.Visibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class ItineraryMapperTest {

    @Test
    fun `maps ownership visibility and stop types from backend response`() {
        val dto = ItineraryResponseDto(
            id = "42",
            ownerId = "owner-1",
            title = "Dolomiti",
            visibility = "SHARED",
            canEdit = true,
            startDate = "2026-07-10",
            endDate = "2026-07-15",
            stops = listOf(
                StopResponseDto(
                    id = "1",
                    stopOrder = 1,
                    stopType = "TRIP",
                    viaggioId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    customTitle = "Trekking",
                    startDatetime = "2026-07-10T09:00:00",
                    endDatetime = "2026-07-10T12:00:00"
                ),
                StopResponseDto(
                    id = "2",
                    stopOrder = 2,
                    stopType = "ACTIVITY",
                    attivitaId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    customTitle = "Visita",
                    startDatetime = "2026-07-10T14:00:00",
                    endDatetime = "2026-07-10T16:00:00"
                ),
                StopResponseDto(
                    id = "3",
                    stopOrder = 3,
                    stopType = "CUSTOM",
                    customTitle = "Cena",
                    startDatetime = "2026-07-10T20:00:00",
                    endDatetime = "2026-07-10T22:00:00"
                )
            )
        )

        val summary = ItineraryMapper.toSummary(dto)
        assertEquals("owner-1", summary.ownerId)
        assertEquals(Visibility.SHARED, summary.visibility)
        assertEquals(true, summary.canEdit)
        assertEquals(3, summary.stagesCount)
        assertEquals(StopType.TRIP, summary.previewStages[0].stopType)
        assertEquals(StopType.ACTIVITY, summary.previewStages[1].stopType)
        assertEquals(StopType.CUSTOM, summary.previewStages[2].stopType)
        assertTrue(summary.previewStages[0].isFromCatalog)
        assertFalse(summary.previewStages[2].isFromCatalog)

        val detail = ItineraryMapper.toDetail(dto)
        assertEquals(Visibility.SHARED, detail.visibility)
        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", detail.stages[0].viaggioId)
        assertNull(detail.stages[0].attivitaId)
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", detail.stages[1].attivitaId)
        assertEquals("Cena", detail.stages[2].title)
    }

    @Test
    fun `maps catalog trip and activity to the correct backend fields`() {
        val tripId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        val activityId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        val baseRequest = CreateStageRequest(
            dayNumber = 1,
            date = LocalDate.of(2026, 7, 10),
            startTime = "09:00",
            endTime = "12:00",
            source = StageSource.Custom("Titolo", "Descrizione", null),
            notes = null
        )

        val tripDto = ItineraryMapper.toDto(
            baseRequest.copy(source = StageSource.FromCatalog(tripId, StopType.TRIP))
        )
        assertEquals("TRIP", tripDto.stopType)
        assertEquals(tripId.toString(), tripDto.viaggioId)
        assertNull(tripDto.attivitaId)

        val activityDto = ItineraryMapper.toDto(
            baseRequest.copy(source = StageSource.FromCatalog(activityId, StopType.ACTIVITY))
        )
        assertEquals("ACTIVITY", activityDto.stopType)
        assertEquals(activityId.toString(), activityDto.attivitaId)
        assertNull(activityDto.viaggioId)

        val customDto = ItineraryMapper.toDto(baseRequest)
        assertEquals("CUSTOM", customDto.stopType)
        assertEquals("Titolo", customDto.customTitle)
    }

    @Test
    fun `maps public token of public itineraries`() {
        val dto = ItineraryResponseDto(
            id = "7",
            title = "Sicilia",
            visibility = "PUBLIC",
            startDate = "2030-10-01",
            endDate = "2030-10-10",
            publicToken = "3f2c7a1e-0000-0000-0000-000000000001",
        )

        assertEquals("3f2c7a1e-0000-0000-0000-000000000001", ItineraryMapper.toDetail(dto).publicToken)
    }

    @Test
    fun `catalog stop stores the catalog item name as title`() {
        val request = CreateStageRequest(
            dayNumber = 1,
            date = LocalDate.of(2030, 10, 8),
            startTime = "10:00",
            endTime = "12:00",
            source = StageSource.FromCatalog(
                catalogItemId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                stopType = StopType.ACTIVITY,
                title = "Galleria degli Uffizi",
            ),
            notes = null,
        )

        val dto = ItineraryMapper.toDto(request)

        assertEquals("Galleria degli Uffizi", dto.customTitle)
        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", dto.attivitaId)
    }

    @Test
    fun `catalog stop without name falls back to a type specific title`() {
        val stage = ItineraryMapper.toStage(
            StopResponseDto(id = "9", stopOrder = 1, stopType = "ACTIVITY", attivitaId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            LocalDate.of(2030, 10, 8),
        )

        assertEquals("Attività dal catalogo", stage.title)
    }
}
