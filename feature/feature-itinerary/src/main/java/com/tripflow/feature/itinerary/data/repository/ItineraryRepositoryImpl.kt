package com.tripflow.feature.itinerary.data.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.data.mapper.ItineraryMapper
import com.tripflow.feature.itinerary.data.remote.ItineraryApi
import com.tripflow.feature.itinerary.data.remote.dto.CatalogItemResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.CatalogSearchResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateStageRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.ItinerarySummaryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StageDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateStageRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.UUID

class ItineraryRepositoryImpl(
    private val api: ItineraryApi,
    private val authTokenProvider: () -> String,
    private val mapper: ItineraryMapper = ItineraryMapper
) : ItineraryRepository {

    override suspend fun getMyItineraries(): UiState<List<ItinerarySummary>> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val response = api.getMyItineraries(authHeader)
            val domainList = response.map { mapper.toDomain(it) }
            UiState.Success(domainList)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getItineraryDetail(id: UUID): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val response = api.getItineraryDetail(id, authHeader)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun createItinerary(
        title: String,
        description: String?,
        startDate: String,
        endDate: String,
        isPublic: Boolean
    ): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val requestDto = CreateItineraryRequestDto(
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                isPublic = isPublic
            )
            val response = api.createItinerary(authHeader, requestDto)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteItinerary(id: UUID): UiState<Unit> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            api.deleteItinerary(id, authHeader)
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateVisibility(id: UUID, isPublic: Boolean): UiState<ItineraryDetail> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val requestDto = UpdateVisibilityRequestDto(isPublic = isPublic)
            val response = api.updateVisibility(id, authHeader, requestDto)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun addStage(itineraryId: UUID, request: CreateStageRequest): UiState<StageDetail> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val requestDto = mapper.toDto(request)
            val response = api.addStage(itineraryId, authHeader, requestDto)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateStage(
        itineraryId: UUID,
        stageId: UUID,
        request: UpdateStageRequest
    ): UiState<StageDetail> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val requestDto = mapper.toDto(request)
            val response = api.updateStage(itineraryId, stageId, authHeader, requestDto)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteStage(itineraryId: UUID, stageId: UUID): UiState<Unit> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            api.deleteStage(itineraryId, stageId, authHeader)
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val response = api.searchCatalog(
                authorization = authHeader,
                query = request.query,
                category = request.category,
                location = request.location,
                page = request.page,
                size = request.size
            )
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getCatalogItem(id: UUID): UiState<CatalogItem> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${authTokenProvider()}"
            val response = api.getCatalogItem(id, authHeader)
            val domain = mapper.toDomain(response)
            UiState.Success(domain)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun <T> handleError(e: Exception): UiState<T> {
        return when (e) {
            is HttpException -> {
                val code = e.code()
                val message = when (code) {
                    400 -> "Richiesta non valida: controlla i dati inseriti"
                    401 -> "Sessione scaduta, effettua di nuovo l'accesso"
                    403 -> "Non hai i permessi per questa operazione"
                    404 -> "Risorsa non trovata"
                    409 -> "Conflitto: la risorsa esiste già"
                    500 -> "Errore interno del server"
                    503 -> "Servizio temporaneamente non disponibile"
                    else -> "Errore del server ($code)"
                }
                val nonRetryableCodes = setOf(400, 401, 403, 404, 409)
                UiState.Error(message, retryable = !nonRetryableCodes.contains(code))
            }
            else -> UiState.Error(e.message ?: "Errore di rete o connessione", retryable = true)
        }
    }
}