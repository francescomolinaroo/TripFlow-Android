package com.tripflow.feature.itinerary.data.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.data.mapper.ItineraryMapper
import com.tripflow.feature.itinerary.data.remote.ApiErrorHandler
import com.tripflow.feature.itinerary.data.remote.ItineraryApi
import com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateItineraryRequest
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import com.tripflow.feature.itinerary.model.UpdateVisibilityRequest
import com.tripflow.feature.itinerary.model.Visibility
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import com.tripflow.feature.itinerary.session.TokenRefresher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.UUID

class ItineraryRepositoryImpl(
    private val api: ItineraryApi,
    private val mapper: ItineraryMapper = ItineraryMapper,
    private val tokenRefresher: TokenRefresher = TokenRefresher.None,
) : ItineraryRepository {

    override suspend fun getMyItineraries(): UiState<List<ItinerarySummary>> = withContext(Dispatchers.IO) {
        try {
            val response = authorized { api.getMyItineraries() }
            UiState.Success(response.map { mapper.toSummary(it) })
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getItineraryDetail(id: String): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val response = authorized { api.getItineraryDetail(id) }
            UiState.Success(mapper.toDetail(response))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun createItinerary(
        request: CreateItineraryRequest
    ): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val requestDto = CreateItineraryRequestDto(
                title = request.title,
                description = request.description,
                visibility = request.visibility.name,
                startDate = request.startDate,
                endDate = request.endDate
            )
            val response = authorized { api.createItinerary(requestDto) }
            UiState.Success(mapper.toDetail(response))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteItinerary(id: String): UiState<Unit> = withContext(Dispatchers.IO) {
        try {
            authorized { api.deleteItinerary(id) }
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateVisibility(id: String, visibility: Visibility): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val requestDto = mapper.toDto(UpdateVisibilityRequest(visibility))
            val response = authorized { api.updateVisibility(id, requestDto) }
            UiState.Success(mapper.toDetail(response))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun addStage(itineraryId: String, request: CreateStageRequest): UiState<StageDetail> = withContext(Dispatchers.IO) {
        try {
            val requestDto = mapper.toDto(request)
            val response = authorized { api.addStop(itineraryId, requestDto) }
            UiState.Success(mapper.toStage(response, null))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateStage(
        itineraryId: String,
        stageId: String,
        request: UpdateStageRequest
    ): UiState<StageDetail> = withContext(Dispatchers.IO) {
        try {
            val requestDto = mapper.toDto(request)
            val response = authorized { api.updateStop(itineraryId, stageId, requestDto) }
            UiState.Success(mapper.toStage(response, null))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteStage(itineraryId: String, stageId: String): UiState<Unit> = withContext(Dispatchers.IO) {
        try {
            authorized { api.deleteStop(itineraryId, stageId) }
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = authorized {
                api.searchCatalog(destination = request.query ?: request.location)
            }
            UiState.Success(mapper.toDomain(response, request))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun searchActivities(query: String): UiState<List<CatalogItem>> = withContext(Dispatchers.IO) {
        try {
            val response = authorized { api.searchActivities(name = query.takeIf { it.isNotBlank() }) }
            UiState.Success(response.map(mapper::toDomain))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getCatalogItem(id: UUID): UiState<CatalogItem> = withContext(Dispatchers.IO) {
        try {
            val response = authorized { api.getCatalogItem(id.toString()) }
            UiState.Success(mapper.toDomain(response))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    // Esegue la chiamata; se il token e' scaduto (401) lo rinnova e riprova una volta.
    private suspend fun <T> authorized(call: suspend () -> T): T {
        val tokenUsed = tokenRefresher.currentToken()
        return try {
            call()
        } catch (e: HttpException) {
            if (e.code() == 401 && tokenRefresher.refreshAfter(tokenUsed)) call() else throw e
        }
    }

    private fun <T> handleError(e: Exception): UiState<T> {
        // La cancellazione della coroutine (es. un ricaricamento che sostituisce il precedente)
        // non e' un errore da mostrare: va propagata.
        if (e is CancellationException) throw e
        val error = ApiErrorHandler.from(e)
        return UiState.Error(error.message, retryable = error.retryable)
    }
}
