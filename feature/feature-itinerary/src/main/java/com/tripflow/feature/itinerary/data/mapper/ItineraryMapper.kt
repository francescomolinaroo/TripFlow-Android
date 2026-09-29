package com.tripflow.feature.itinerary.data.mapper

import com.tripflow.feature.itinerary.data.remote.dto.CatalogItemResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.CatalogSearchResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.ItinerarySummaryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StageDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StagePreviewResponseDto
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.StagePreview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object ItineraryMapper {

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val isoDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun toDomain(dto: ItinerarySummaryResponseDto): ItinerarySummary {
        return ItinerarySummary(
            id = parseUuid(dto.id),
            title = dto.title,
            startDate = parseLocalDate(dto.startDate),
            endDate = parseLocalDate(dto.endDate),
            isPublic = dto.isPublic,
            stagesCount = dto.stagesCount,
            previewStages = dto.previewStages.map { toDomain(it) },
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun toDomain(dto: StagePreviewResponseDto): StagePreview {
        return StagePreview(
            id = parseUuid(dto.id),
            dayNumber = dto.dayNumber,
            title = dto.title,
            startTime = dto.startTime,
            endTime = dto.endTime,
            isFromCatalog = dto.isFromCatalog
        )
    }

    fun toDomain(dto: ItineraryDetailResponseDto): ItineraryDetail {
        return ItineraryDetail(
            id = parseUuid(dto.id),
            title = dto.title,
            description = dto.description,
            startDate = parseLocalDate(dto.startDate),
            endDate = parseLocalDate(dto.endDate),
            isPublic = dto.isPublic,
            stages = dto.stages.map { toDomain(it) },
            totalStages = dto.totalStages,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun toDomain(dto: StageDetailResponseDto): StageDetail {
        return StageDetail(
            id = parseUuid(dto.id),
            dayNumber = dto.dayNumber,
            date = parseLocalDate(dto.date),
            startTime = dto.startTime,
            endTime = dto.endTime,
            title = dto.title,
            description = dto.description,
            location = dto.location,
            isFromCatalog = dto.isFromCatalog,
            catalogItemId = dto.catalogItemId?.let { parseUuid(it) },
            notes = dto.notes,
            orderIndex = dto.orderIndex
        )
    }

    fun toDomain(dto: CatalogItemResponseDto): CatalogItem {
        return CatalogItem(
            id = parseUuid(dto.id),
            title = dto.title,
            description = dto.description,
            location = dto.location,
            category = dto.category,
            durationMinutes = dto.durationMinutes,
            imageUrl = dto.imageUrl,
            averageRating = dto.averageRating
        )
    }

    fun toDomain(dto: CatalogSearchResponseDto): CatalogSearchResponse {
        return CatalogSearchResponse(
            content = dto.content.map { toDomain(it) },
            totalElements = dto.totalElements,
            totalPages = dto.totalPages,
            currentPage = dto.currentPage
        )
    }

    fun toDto(domain: com.tripflow.feature.itinerary.model.CreateItineraryRequest): 
        com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto {
        return com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto(
            title = domain.title,
            description = domain.description,
            startDate = domain.startDate,
            endDate = domain.endDate,
            isPublic = domain.isPublic
        )
    }

    fun toDto(domain: com.tripflow.feature.itinerary.model.UpdateVisibilityRequest): 
        com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto {
        return com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto(
            isPublic = domain.isPublic
        )
    }

    fun toDto(domain: com.tripflow.feature.itinerary.model.CreateStageRequest): 
        com.tripflow.feature.itinerary.data.remote.dto.CreateStageRequestDto {
        return com.tripflow.feature.itinerary.data.remote.dto.CreateStageRequestDto(
            dayNumber = domain.dayNumber,
            date = domain.date.format(isoDateFormatter),
            startTime = domain.startTime,
            endTime = domain.endTime,
            source = when (domain.source) {
                is com.tripflow.feature.itinerary.model.StageSource.FromCatalog -> 
                    com.tripflow.feature.itinerary.data.remote.dto.StageSourceRequestDto.FromCatalog(
                        catalogItemId = domain.source.catalogItemId.toString()
                    )
                is com.tripflow.feature.itinerary.model.StageSource.Custom -> 
                    com.tripflow.feature.itinerary.data.remote.dto.StageSourceRequestDto.Custom(
                        title = domain.source.title,
                        description = domain.source.description,
                        location = domain.source.location
                    )
            },
            notes = domain.notes
        )
    }

    fun toDto(domain: com.tripflow.feature.itinerary.model.UpdateStageRequest): 
        com.tripflow.feature.itinerary.data.remote.dto.UpdateStageRequestDto {
        return com.tripflow.feature.itinerary.data.remote.dto.UpdateStageRequestDto(
            startTime = domain.startTime,
            endTime = domain.endTime,
            title = domain.title,
            description = domain.description,
            location = domain.location,
            notes = domain.notes,
            orderIndex = domain.orderIndex
        )
    }

    private fun parseUuid(uuidString: String): UUID {
        return try {
            UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            UUID.randomUUID()
        }
    }

    private fun parseLocalDate(dateString: String): LocalDate {
        return try {
            LocalDate.parse(dateString, isoDateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
}