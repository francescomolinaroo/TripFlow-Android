package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateItineraryRequest
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StagePreview
import com.tripflow.feature.itinerary.model.StageSource
import com.tripflow.feature.itinerary.model.StopType
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import com.tripflow.feature.itinerary.model.Visibility
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.min

class FakeItineraryRepository : ItineraryRepository {

    private val mockItineraries = mutableListOf(
        ItinerarySummary(
            id = "1",
            ownerId = "owner-1",
            title = "Costa Amalfitana",
            startDate = LocalDate.of(2026, 9, 14),
            endDate = LocalDate.of(2026, 9, 18),
            visibility = Visibility.PRIVATE,
            canEdit = true,
            stagesCount = 12,
            previewStages = listOf(
                StagePreview(
                    id = "101",
                    dayNumber = 1,
                    title = "Arrivo ad Amalfi e check-in",
                    startTime = "14:00",
                    endTime = "16:00",
                    stopType = StopType.CUSTOM
                ),
                StagePreview(
                    id = "102",
                    dayNumber = 1,
                    title = "Passeggiata sul lungomare",
                    startTime = "17:00",
                    endTime = "19:00",
                    stopType = StopType.ACTIVITY
                ),
                StagePreview(
                    id = "103",
                    dayNumber = 2,
                    title = "Escursione a Positano",
                    startTime = "09:00",
                    endTime = "13:00",
                    stopType = StopType.TRIP
                )
            ),
            createdAt = "2026-01-15T10:00:00Z",
            updatedAt = "2026-01-15T10:00:00Z"
        ),
        ItinerarySummary(
            id = "2",
            ownerId = "owner-2",
            title = "Trekking Dolomiti",
            startDate = LocalDate.of(2026, 7, 10),
            endDate = LocalDate.of(2026, 7, 15),
            visibility = Visibility.SHARED,
            canEdit = true,
            stagesCount = 8,
            previewStages = listOf(
                StagePreview(
                    id = "201",
                    dayNumber = 1,
                    title = "Arrivo a Ortisei",
                    startTime = "12:00",
                    endTime = "14:00",
                    stopType = StopType.CUSTOM
                ),
                StagePreview(
                    id = "202",
                    dayNumber = 2,
                    title = "Alpe di Siusi - Escursione guidata",
                    startTime = "08:30",
                    endTime = "13:00",
                    stopType = StopType.ACTIVITY
                )
            ),
            createdAt = "2026-02-01T10:00:00Z",
            updatedAt = "2026-02-01T10:00:00Z"
        ),
        ItinerarySummary(
            id = "3",
            ownerId = "owner-1",
            title = "Sicilia on the road",
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 10),
            visibility = Visibility.PUBLIC,
            canEdit = true,
            stagesCount = 15,
            previewStages = listOf(
                StagePreview(
                    id = "301",
                    dayNumber = 1,
                    title = "Catania - Ritiro auto",
                    startTime = "10:00",
                    endTime = "11:00",
                    stopType = StopType.CUSTOM
                ),
                StagePreview(
                    id = "302",
                    dayNumber = 1,
                    title = "Visita Etna",
                    startTime = "12:00",
                    endTime = "17:00",
                    stopType = StopType.ACTIVITY
                ),
                StagePreview(
                    id = "303",
                    dayNumber = 2,
                    title = "Taormina e Isola Bella",
                    startTime = "09:00",
                    endTime = "14:00",
                    stopType = StopType.TRIP
                )
            ),
            createdAt = "2026-03-01T10:00:00Z",
            updatedAt = "2026-03-01T10:00:00Z"
        )
    )

    private val mockCatalog = listOf(
        CatalogItem(
            id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            title = "Escursione guidata Etna",
            description = "Trekking sul vulcano attivo piu alto d'Europa con guida alpina certificata",
            location = "Catania, Sicilia",
            category = "NATURA",
            durationMinutes = 300,
            imageUrl = null,
            averageRating = 4.8
        ),
        CatalogItem(
            id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            title = "Tour in barca Costiera Amalfitana",
            description = "Giro in barca privata con soste per bagno a Positano, Amalfi e Ravello",
            location = "Amalfi, Campania",
            category = "MARE",
            durationMinutes = 480,
            imageUrl = null,
            averageRating = 4.9
        ),
        CatalogItem(
            id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            title = "Degustazione vini Valpolicella",
            description = "Visita cantina storica con degustazione Amarone e Recioto",
            location = "Verona, Veneto",
            category = "CIBO",
            durationMinutes = 180,
            imageUrl = null,
            averageRating = 4.7
        ),
        CatalogItem(
            id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
            title = "Alpe di Siusi - Escursione guidata",
            description = "Passeggiata sull'altopiano piu grande d'Europa con vista Dolomiti",
            location = "Ortisei, Trentino-Alto Adige",
            category = "NATURA",
            durationMinutes = 270,
            imageUrl = null,
            averageRating = 4.6
        ),
        CatalogItem(
            id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
            title = "Cooking class pasta fatta in casa",
            description = "Impara a fare orecchiette e tagliatelle con una nonna pugliese",
            location = "Ostuni, Puglia",
            category = "CIBO",
            durationMinutes = 240,
            imageUrl = null,
            averageRating = 4.9
        ),
        CatalogItem(
            id = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            title = "Kayak alle Isole Eolie",
            description = "Escursione in kayak tra Vulcano, Lipari e Salina",
            location = "Lipari, Sicilia",
            category = "SPORT",
            durationMinutes = 360,
            imageUrl = null,
            averageRating = 4.5
        )
    )

    override suspend fun getMyItineraries(): UiState<List<ItinerarySummary>> {
        return UiState.Success(mockItineraries.toList())
    }

    override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> {
        val summary = mockItineraries.find { it.id == id }
        if (summary == null) return UiState.Error("Itinerario non trovato")

        val stages = generateMockStages(summary)

        return UiState.Success(
            ItineraryDetail(
                id = summary.id,
                ownerId = summary.ownerId,
                title = summary.title,
                description = "Itinerario di esempio con tappe dettagliate",
                startDate = summary.startDate,
                endDate = summary.endDate,
                visibility = summary.visibility,
                canEdit = summary.canEdit,
                stages = stages,
                totalStages = stages.size,
                createdAt = summary.createdAt,
                updatedAt = summary.updatedAt
            )
        )
    }

    override suspend fun createItinerary(
        request: CreateItineraryRequest
    ): UiState<ItineraryDetail> {
        val newId = ((mockItineraries.mapNotNull { it.id.toLongOrNull() }.maxOrNull() ?: 0L) + 1L).toString()
        val summary = ItinerarySummary(
            id = newId,
            ownerId = "owner-1",
            title = request.title,
            startDate = LocalDate.parse(request.startDate),
            endDate = LocalDate.parse(request.endDate),
            visibility = request.visibility,
            canEdit = true,
            stagesCount = 0,
            previewStages = emptyList(),
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        mockItineraries.add(summary)

        return UiState.Success(
            ItineraryDetail(
                id = newId,
                ownerId = summary.ownerId,
                title = request.title,
                description = request.description,
                startDate = LocalDate.parse(request.startDate),
                endDate = LocalDate.parse(request.endDate),
                visibility = request.visibility,
                canEdit = true,
                stages = emptyList(),
                totalStages = 0,
                createdAt = summary.createdAt,
                updatedAt = summary.updatedAt
            )
        )
    }

    override suspend fun deleteItinerary(id: String): UiState<Unit> {
        mockItineraries.removeIf { it.id == id }
        return UiState.Success(Unit)
    }

    override suspend fun updateVisibility(id: String, visibility: Visibility): UiState<ItineraryDetail> {
        val index = mockItineraries.indexOfFirst { it.id == id }
        if (index == -1) return UiState.Error("Itinerario non trovato")

        val old = mockItineraries[index]
        mockItineraries[index] = old.copy(
            visibility = visibility,
            updatedAt = java.time.Instant.now().toString()
        )

        return getItineraryDetail(id)
    }

    override suspend fun addStage(itineraryId: String, request: CreateStageRequest): UiState<StageDetail> {
        val source = request.source
        val stopType = when (source) {
            is StageSource.Custom -> StopType.CUSTOM
            is StageSource.FromCatalog -> source.stopType
        }
        val catalogId = (source as? StageSource.FromCatalog)?.catalogItemId?.toString()
        val title = when (source) {
            is StageSource.Custom -> source.title
            is StageSource.FromCatalog -> source.title ?: "Tappa dal catalogo"
        }
        val description = when (source) {
            is StageSource.Custom -> source.description
            is StageSource.FromCatalog -> null
        }
        val location = when (source) {
            is StageSource.Custom -> source.location
            is StageSource.FromCatalog -> null
        }

        val newStage = StageDetail(
            id = UUID.randomUUID().toString(),
            dayNumber = request.dayNumber,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            title = title,
            description = description,
            location = location,
            stopType = stopType,
            viaggioId = catalogId.takeIf { stopType == StopType.TRIP },
            attivitaId = catalogId.takeIf { stopType == StopType.ACTIVITY },
            notes = request.notes,
            orderIndex = request.orderIndex ?: 0
        )
        return UiState.Success(newStage)
    }

    override suspend fun updateStage(
        itineraryId: String,
        stageId: String,
        request: UpdateStageRequest
    ): UiState<StageDetail> {
        val stopType = request.stopType ?: StopType.CUSTOM
        return UiState.Success(
            StageDetail(
                id = stageId,
                dayNumber = 1,
                date = request.date ?: LocalDate.now(),
                startTime = request.startTime ?: "09:00",
                endTime = request.endTime ?: "12:00",
                title = request.title ?: "Tappa aggiornata",
                description = request.description,
                location = request.location,
                stopType = stopType,
                viaggioId = request.viaggioId,
                attivitaId = request.attivitaId,
                notes = request.notes,
                orderIndex = request.orderIndex ?: 0
            )
        )
    }

    override suspend fun deleteStage(itineraryId: String, stageId: String): UiState<Unit> {
        return UiState.Success(Unit)
    }

    override suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse> {
        var filtered = mockCatalog

        if (request.query?.isNotBlank() == true) {
            val q = request.query.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.location.lowercase().contains(q)
            }
        }
        if (request.category?.isNotBlank() == true) {
            filtered = filtered.filter { it.category == request.category }
        }
        if (request.location?.isNotBlank() == true) {
            filtered = filtered.filter { it.location.lowercase().contains(request.location!!.lowercase()) }
        }

        val page = request.page
        val size = request.size
        val start = page * size
        val end = min(start + size, filtered.size)
        val paged = if (start < filtered.size) filtered.subList(start, end) else emptyList()

        return UiState.Success(
            CatalogSearchResponse(
                content = paged,
                totalElements = filtered.size.toLong(),
                totalPages = (filtered.size + size - 1) / size,
                currentPage = page
            )
        )
    }

    override suspend fun searchActivities(query: String): UiState<List<CatalogItem>> {
        val normalized = query.trim().lowercase()
        val results = mockCatalog.filter { item ->
            normalized.isEmpty() ||
                item.title.lowercase().contains(normalized) ||
                item.description.lowercase().contains(normalized)
        }
        return UiState.Success(results)
    }

    override suspend fun getCatalogItem(id: UUID): UiState<CatalogItem> {
        val item = mockCatalog.find { it.id == id }
        return if (item != null) UiState.Success(item) else UiState.Error("Elemento catalogo non trovato")
    }

    private fun generateMockStages(summary: ItinerarySummary): List<StageDetail> {
        val days = summary.startDate.until(summary.endDate.plusDays(1), ChronoUnit.DAYS).toInt()
        var counter = 0
        return (1..days).flatMap { day ->
            val date = summary.startDate.plusDays((day - 1).toLong())
            val stageCount = 3
            (1..stageCount).map { stageIndex ->
                counter += 1
                val stopType = when (stageIndex) {
                    2 -> StopType.TRIP
                    3 -> StopType.ACTIVITY
                    else -> StopType.CUSTOM
                }
                val catalogId = if (stopType == StopType.CUSTOM) null else UUID.randomUUID().toString()
                StageDetail(
                    id = counter.toString(),
                    dayNumber = day,
                    date = date,
                    startTime = when (stageIndex) {
                        1 -> "09:00"
                        2 -> "14:00"
                        else -> "18:00"
                    },
                    endTime = when (stageIndex) {
                        1 -> "12:00"
                        2 -> "17:00"
                        else -> "20:00"
                    },
                    title = "Tappa $stageIndex - Giorno $day",
                    description = "Descrizione dettagliata della tappa",
                    location = "Luogo $day",
                    stopType = stopType,
                    viaggioId = catalogId.takeIf { stopType == StopType.TRIP },
                    attivitaId = catalogId.takeIf { stopType == StopType.ACTIVITY },
                    notes = "Note personali per la tappa",
                    orderIndex = stageIndex - 1
                )
            }
        }
    }
}
