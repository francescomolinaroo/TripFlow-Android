package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import java.util.UUID

interface ItineraryRepository {

    suspend fun getMyItineraries(): UiState<List<ItinerarySummary>>

    suspend fun getItineraryDetail(id: UUID): UiState<ItineraryDetail>

    suspend fun createItinerary(
        title: String,
        description: String?,
        startDate: String,
        endDate: String,
        isPublic: Boolean
    ): UiState<ItineraryDetail>

    suspend fun deleteItinerary(id: UUID): UiState<Unit>

    suspend fun updateVisibility(id: UUID, isPublic: Boolean): UiState<ItineraryDetail>

    suspend fun addStage(itineraryId: UUID, request: CreateStageRequest): UiState<StageDetail>

    suspend fun updateStage(
        itineraryId: UUID,
        stageId: UUID,
        request: UpdateStageRequest
    ): UiState<StageDetail>

    suspend fun deleteStage(itineraryId: UUID, stageId: UUID): UiState<Unit>

    suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse>

    suspend fun getCatalogItem(id: UUID): UiState<CatalogItem>
}