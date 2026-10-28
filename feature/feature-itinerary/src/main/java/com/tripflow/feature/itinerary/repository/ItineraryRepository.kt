package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateItineraryRequest
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItineraryShare
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import com.tripflow.feature.itinerary.model.Visibility
import java.util.UUID

interface ItineraryRepository {

    suspend fun getMyItineraries(): UiState<List<ItinerarySummary>>

    suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail>

    suspend fun createItinerary(request: CreateItineraryRequest): UiState<ItineraryDetail>

    suspend fun deleteItinerary(id: String): UiState<Unit>

    suspend fun updateVisibility(id: String, visibility: Visibility): UiState<ItineraryDetail>

    suspend fun addStage(itineraryId: String, request: CreateStageRequest): UiState<StageDetail>

    suspend fun updateStage(
        itineraryId: String,
        stageId: String,
        request: UpdateStageRequest
    ): UiState<StageDetail>

    suspend fun deleteStage(itineraryId: String, stageId: String): UiState<Unit>

    suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse>

    suspend fun searchActivities(query: String): UiState<List<CatalogItem>>

    suspend fun getCatalogItem(id: UUID): UiState<CatalogItem>

    // Condivisioni con utenti specifici. Implementazione di default per i repository
    // finti (Preview/test) che non le supportano.
    suspend fun getShares(itineraryId: String): UiState<List<ItineraryShare>> =
        UiState.Error("Condivisione non disponibile", retryable = false)

    suspend fun shareItinerary(itineraryId: String, userId: String, canEdit: Boolean): UiState<ItineraryShare> =
        UiState.Error("Condivisione non disponibile", retryable = false)

    suspend fun removeShare(itineraryId: String, userId: String): UiState<Unit> =
        UiState.Error("Condivisione non disponibile", retryable = false)
}
