package com.tripflow.feature.itinerary.data.mapper

import com.tripflow.feature.itinerary.data.remote.dto.ActivityResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.AddStopRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StopResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.TripResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateStopRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto
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
import com.tripflow.feature.itinerary.model.UpdateVisibilityRequest
import com.tripflow.feature.itinerary.model.Visibility
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

object ItineraryMapper {

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private const val PREVIEW_STAGES = 3

    fun toSummary(dto: ItineraryResponseDto): ItinerarySummary {
        val startDate = parseDate(dto.startDate)
        return ItinerarySummary(
            id = dto.id,
            ownerId = dto.ownerId,
            title = dto.title,
            startDate = startDate,
            endDate = parseDate(dto.endDate),
            visibility = parseVisibility(dto.visibility),
            canEdit = dto.canEdit,
            stagesCount = dto.stops.size,
            previewStages = dto.stops.take(PREVIEW_STAGES).map { toPreview(it, startDate) },
            createdAt = dto.createdAt.orEmpty(),
            updatedAt = dto.updatedAt.orEmpty()
        )
    }

    fun toDetail(dto: ItineraryResponseDto): ItineraryDetail {
        val startDate = parseDate(dto.startDate)
        return ItineraryDetail(
            id = dto.id,
            ownerId = dto.ownerId,
            title = dto.title,
            description = dto.description,
            startDate = startDate,
            endDate = parseDate(dto.endDate),
            visibility = parseVisibility(dto.visibility),
            canEdit = dto.canEdit,
            stages = dto.stops.map { toStage(it, startDate) },
            totalStages = dto.stops.size,
            createdAt = dto.createdAt.orEmpty(),
            updatedAt = dto.updatedAt.orEmpty(),
            publicToken = dto.publicToken
        )
    }

    fun toPreview(dto: StopResponseDto, itineraryStart: LocalDate?): StagePreview {
        val start = parseDateTime(dto.startDatetime)
        val stopType = parseStopType(dto.stopType, dto.viaggioId, dto.attivitaId)
        return StagePreview(
            id = dto.id,
            dayNumber = dayNumber(itineraryStart, start?.toLocalDate()),
            title = dto.customTitle ?: titleFor(stopType),
            startTime = start?.toLocalTime()?.format(timeFormatter).orEmpty(),
            endTime = parseDateTime(dto.endDatetime)?.toLocalTime()?.format(timeFormatter).orEmpty(),
            stopType = stopType
        )
    }

    fun toStage(dto: StopResponseDto, itineraryStart: LocalDate?): StageDetail {
        val start = parseDateTime(dto.startDatetime)
        val date = start?.toLocalDate() ?: itineraryStart ?: LocalDate.now()
        val stopType = parseStopType(dto.stopType, dto.viaggioId, dto.attivitaId)
        return StageDetail(
            id = dto.id,
            dayNumber = dayNumber(itineraryStart, date),
            date = date,
            startTime = start?.toLocalTime()?.format(timeFormatter).orEmpty(),
            endTime = parseDateTime(dto.endDatetime)?.toLocalTime()?.format(timeFormatter).orEmpty(),
            title = dto.customTitle ?: titleFor(stopType),
            description = dto.customDescription,
            location = null,
            stopType = stopType,
            viaggioId = dto.viaggioId,
            attivitaId = dto.attivitaId,
            notes = dto.notes,
            orderIndex = dto.stopOrder ?: 0
        )
    }

    fun toDto(domain: CreateItineraryRequest): CreateItineraryRequestDto {
        return CreateItineraryRequestDto(
            title = domain.title,
            description = domain.description,
            visibility = domain.visibility.name,
            startDate = domain.startDate,
            endDate = domain.endDate
        )
    }

    fun toDto(domain: UpdateVisibilityRequest): UpdateVisibilityRequestDto {
        return UpdateVisibilityRequestDto(visibility = domain.visibility.name)
    }

    fun toDto(domain: CreateStageRequest): AddStopRequestDto {
        val catalog = domain.source as? StageSource.FromCatalog
        val custom = domain.source as? StageSource.Custom
        val stopType = catalog?.stopType ?: StopType.CUSTOM
        val catalogId = catalog?.catalogItemId?.toString()
        return AddStopRequestDto(
            stopOrder = domain.orderIndex ?: domain.dayNumber,
            stopType = stopType.name,
            viaggioId = catalogId.takeIf { stopType == StopType.TRIP },
            attivitaId = catalogId.takeIf { stopType == StopType.ACTIVITY },
            customTitle = custom?.title ?: catalog?.title?.takeIf { it.isNotBlank() }?.take(150),
            customDescription = custom?.description,
            startDatetime = combine(domain.date, domain.startTime),
            endDatetime = combine(domain.date, domain.endTime),
            notes = domain.notes
        )
    }

    fun toDto(domain: UpdateStageRequest): UpdateStopRequestDto {
        val startDatetime = domain.date
            ?.takeIf { domain.startTime != null }
            ?.let { combine(it, domain.startTime.orEmpty()) }
        val endDatetime = domain.date
            ?.takeIf { domain.endTime != null }
            ?.let { combine(it, domain.endTime.orEmpty()) }
        return UpdateStopRequestDto(
            stopOrder = domain.orderIndex,
            stopType = domain.stopType?.name,
            viaggioId = domain.viaggioId,
            attivitaId = domain.attivitaId,
            customTitle = domain.title,
            customDescription = domain.description,
            startDatetime = startDatetime,
            endDatetime = endDatetime,
            notes = domain.notes
        )
    }

    fun toDomain(dto: TripResponseDto): CatalogItem {
        return CatalogItem(
            id = parseUuidOrNull(dto.id) ?: UUID.randomUUID(),
            title = dto.name,
            description = dto.description.orEmpty(),
            location = dto.destination.orEmpty(),
            category = "",
            durationMinutes = 0,
            imageUrl = dto.images?.firstOrNull(),
            averageRating = dto.rating
        )
    }

    fun toDomain(dto: ActivityResponseDto): CatalogItem {
        return CatalogItem(
            id = parseUuidOrNull(dto.id) ?: UUID.randomUUID(),
            title = dto.name,
            description = dto.description.orEmpty(),
            location = "",
            category = "ACTIVITY",
            durationMinutes = dto.duration ?: 0,
            imageUrl = null,
            averageRating = null
        )
    }

    fun toDomain(dtos: List<TripResponseDto>, request: CatalogSearchRequest): CatalogSearchResponse {
        val items = dtos.map { toDomain(it) }
        return CatalogSearchResponse(
            content = items,
            totalElements = items.size.toLong(),
            totalPages = 1,
            currentPage = request.page
        )
    }

    private fun parseVisibility(value: String?): Visibility {
        return Visibility.entries.firstOrNull {
            it.name == value?.uppercase(Locale.ROOT)
        } ?: Visibility.PRIVATE
    }

    private fun parseStopType(
        value: String?,
        viaggioId: String?,
        attivitaId: String?
    ): StopType {
        return StopType.entries.firstOrNull {
            it.name == value?.uppercase(Locale.ROOT)
        } ?: when {
            viaggioId != null -> StopType.TRIP
            attivitaId != null -> StopType.ACTIVITY
            else -> StopType.CUSTOM
        }
    }

    // Titolo di ripiego per le tappe da catalogo salvate senza nome (create prima del fix).
    private fun titleFor(stopType: StopType): String = when (stopType) {
        StopType.TRIP -> "Viaggio dal catalogo"
        StopType.ACTIVITY -> "Attività dal catalogo"
        StopType.CUSTOM -> ""
    }

    private fun dayNumber(start: LocalDate?, date: LocalDate?): Int {
        if (start == null || date == null) return 1
        val diff = ChronoUnit.DAYS.between(start, date).toInt() + 1
        return if (diff < 1) 1 else diff
    }

    private fun combine(date: LocalDate, time: String): String {
        val normalizedTime = if (time.length == 5) "$time:00" else time
        return "${date.format(isoDateFormatter)}T$normalizedTime"
    }

    private fun parseDate(value: String?): LocalDate {
        return try {
            LocalDate.parse(value?.take(10), isoDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        if (value == null) return null
        return try {
            LocalDateTime.parse(value, isoDateTimeFormatter)
        } catch (e: Exception) {
            try {
                LocalDate.parse(value.take(10), isoDateFormatter).atStartOfDay()
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parseUuidOrNull(value: String?): UUID? {
        if (value == null) return null
        return try {
            UUID.fromString(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
