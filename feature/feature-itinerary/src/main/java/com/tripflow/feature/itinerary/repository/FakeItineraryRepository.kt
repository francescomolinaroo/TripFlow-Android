package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StagePreview
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import java.time.LocalDate
import java.util.UUID

class FakeItineraryRepository : ItineraryRepository {

    private val mockItineraries = mutableListOf(
        ItinerarySummary(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            title = "Costa Amalfitana",
            startDate = LocalDate.of(2026, 9, 14),
            endDate = LocalDate.of(2026, 9, 18),
            isPublic = true,
            stagesCount = 12,
            previewStages = listOf(
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 1,
                    title = "Arrivo ad Amalfi e check-in",
                    startTime = "14:00",
                    endTime = "16:00",
                    isFromCatalog = false
                ),
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 1,
                    title = "Passeggiata sul lungomare",
                    startTime = "17:00",
                    endTime = "19:00",
                    isFromCatalog = true
                ),
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 2,
                    title = "Escursione a Positano",
                    startTime = "09:00",
                    endTime = "13:00",
                    isFromCatalog = true
                )
            ),
            createdAt = "2026-01-15T10:00:00Z",
            updatedAt = "2026-01-15T10:00:00Z"
        ),
        ItinerarySummary(
            id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
            title = "Trekking Dolomiti",
            startDate = LocalDate.of(2026, 7, 10),
            endDate = LocalDate.of(2026, 7, 15),
            isPublic = false,
            stagesCount = 8,
            previewStages = listOf(
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 1,
                    title = "Arrivo a Ortisei",
                    startTime = "12:00",
                    endTime = "14:00",
                    isFromCatalog = false
                ),
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 2,
                    title = "Alpe di Siusi - Escursione guidata",
                    startTime = "08:30",
                    endTime = "13:00",
                    isFromCatalog = true
                )
            ),
            createdAt = "2026-02-01T10:00:00Z",
            updatedAt = "2026-02-01T10:00:00Z"
        ),
        ItinerarySummary(
            id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
            title = "Sicilia on the road",
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 10),
            isPublic = true,
            stagesCount = 15,
            previewStages = listOf(
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 1,
                    title = "Catania - Ritiro auto",
                    startTime = "10:00",
                    endTime = "11:00",
                    isFromCatalog = false
                ),
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 1,
                    title = "Visita Etna",
                    startTime = "12:00",
                    endTime = "17:00",
                    isFromCatalog = true
                ),
                StagePreview(
                    id = UUID.randomUUID(),
                    dayNumber = 2,
                    title = "Taormina e Isola Bella",
                    startTime = "09:00",
                    endTime = "14:00",
                    isFromCatalog = true
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
            description = "Trekking sul vulcano attivo più alto d'Europa con guida alpina certificata",
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
            description = "Passeggiata sull'altopiano più grande d'Europa con vista Dolomiti",
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

    override suspend fun getItineraryDetail(id: UUID): UiState<ItineraryDetail> {
        val summary = mockItineraries.find { it.id == id }
        if (summary == null) return UiState.Error("Itinerario non trovato")
        
        val stages = generateMockStages(summary)
        
        return UiState.Success(ItineraryDetail(
            id = summary.id,
            title = summary.title,
            description = "Itinerario di esempio con tappe dettagliate",
            startDate = summary.startDate,
            endDate = summary.endDate,
            isPublic = summary.isPublic,
            stages = stages,
            totalStages = stages.size,
            createdAt = summary.createdAt,
            updatedAt = summary.updatedAt
        ))
    }

    override suspend fun createItinerary(
        title: String,
        description: String?,
        startDate: String,
        endDate: String,
        isPublic: Boolean
    ): UiState<ItineraryDetail> {
        val newId = UUID.randomUUID()
        val summary = ItinerarySummary(
            id = newId,
            title = title,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            isPublic = isPublic,
            stagesCount = 0,
            previewStages = emptyList(),
            createdAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )
        mockItineraries.add(summary)
        
        return UiState.Success(ItineraryDetail(
            id = newId,
            title = title,
            description = description,
            startDate = LocalDate.parse(startDate),
            endDate = LocalDate.parse(endDate),
            isPublic = isPublic,
            stages = emptyList(),
            totalStages = 0,
            createdAt = summary.createdAt,
            updatedAt = summary.updatedAt
        ))
    }

    override suspend fun deleteItinerary(id: UUID): UiState<Unit> {
        mockItineraries.removeIf { it.id == id }
        return UiState.Success(Unit)
    }

    override suspend fun updateVisibility(id: UUID, isPublic: Boolean): UiState<ItineraryDetail> {
        val index = mockItineraries.indexOfFirst { it.id == id }
        if (index == -1) return UiState.Error("Itinerario non trovato")
        
        val old = mockItineraries[index]
        val updated = old.copy(isPublic = isPublic, updatedAt = java.time.Instant.now().toString())
        mockItineraries[index] = updated
        
        return getItineraryDetail(id)
    }

    override suspend fun addStage(itineraryId: UUID, request: CreateStageRequest): UiState<StageDetail> {
        val newStage = StageDetail(
            id = UUID.randomUUID(),
            dayNumber = request.dayNumber,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            title = when (request.source) {
                is CreateStageRequest.StageSource.Custom -> request.source.title
                is CreateStageRequest.StageSource.FromCatalog -> "Da Catalogo"
            },
            description = when (request.source) {
                is CreateStageRequest.StageSource.Custom -> request.source.description
                is CreateStageRequest.StageSource.FromCatalog -> null
            },
            location = when (request.source) {
                is CreateStageRequest.StageSource.Custom -> request.source.location
                is CreateStageRequest.StageSource.FromCatalog -> null
            },
            isFromCatalog = request.source is CreateStageRequest.StageSource.FromCatalog,
            catalogItemId = (request.source as? CreateStageRequest.StageSource.FromCatalog)?.catalogItemId,
            notes = request.notes,
            orderIndex = 0
        )
        return UiState.Success(newStage)
    }

    override suspend fun updateStage(
        itineraryId: UUID,
        stageId: UUID,
        request: UpdateStageRequest
    ): UiState<StageDetail> {
        return UiState.Success(StageDetail(
            id = stageId,
            dayNumber = 1,
            date = LocalDate.now(),
            startTime = request.startTime ?: "09:00",
            endTime = request.endTime ?: "12:00",
            title = request.title ?: "Tappa aggiornata",
            description = request.description,
            location = request.location,
            isFromCatalog = false,
            catalogItemId = null,
            notes = request.notes,
            orderIndex = request.orderIndex ?: 0
        ))
    }

    override suspend fun deleteStage(itineraryId: UUID, stageId: UUID): UiState<Unit> {
        return UiState.Success(Unit)
    }

    override suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse> {
        var filtered = mockCatalog
        
        if (request.query?.isNotBlank() == true) {
            val q = request.query!.lowercase()
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
            filtered = filtered.filter { it.location.lowercase().contains(request.location!.lowercase()) }
        }
        
        val page = request.page
        val size = request.size
        val start = page * size
        val end = min(start + size, filtered.size)
        val paged = if (start < filtered.size) filtered.subList(start, end) else emptyList()
        
        return UiState.Success(CatalogSearchResponse(
            content = paged,
            totalElements = filtered.size.toLong(),
            totalPages = (filtered.size + size - 1) / size,
            currentPage = page
        ))
    }

    override suspend fun getCatalogItem(id: UUID): UiState<CatalogItem> {
        val item = mockCatalog.find { it.id == id }
        return if (item != null) UiState.Success(item) else UiState.Error("Elemento catalogo non trovato")
    }

    private fun generateMockStages(summary: ItinerarySummary): List<StageDetail> {
        val days = summary.startDate.daysUntil(summary.endDate.plusDays(1))
        return (1..days).map { day ->
            val date = summary.startDate.plusDays(day - 1)
            val stageCount = (2..3).random()
            (1..stageCount).map { stageIndex ->
                StageDetail(
                    id = UUID.randomUUID(),
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
                    isFromCatalog = stageIndex % 2 == 0,
                    catalogItemId = if (stageIndex % 2 == 0) UUID.randomUUID() else null,
                    notes = "Note personali per la tappa",
                    orderIndex = stageIndex - 1
                )
            }
        }.flatten()
    }
}