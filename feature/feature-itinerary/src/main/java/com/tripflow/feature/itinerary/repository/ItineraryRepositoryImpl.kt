package com.tripflow.feature.itinerary.repository

import com.tripflow.core.model.UiState
import com.tripflow.feature.itinerary.api.CreateItineraryRequest
import com.tripflow.feature.itinerary.api.ItineraryApi
import com.tripflow.feature.itinerary.api.VisibilityRequest
import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class ItineraryRepositoryImpl : ItineraryRepository {

    private val api: ItineraryApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItineraryApi::class.java)
    }

    override suspend fun getMyItineraries(): UiState<List<ItinerarySummary>> = withContext(ioDispatcher) {
        try {
            val response = api.getMyItineraries()
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getItineraryDetail(id: UUID): UiState<ItineraryDetail> = withContext(ioDispatcher) {
        try {
            val response = api.getItineraryDetail(id)
            UiState.Success(response)
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
    ): UiState<ItineraryDetail> = withContext(ioDispatcher) {
        try {
            val request = CreateItineraryRequest(title, description, startDate, endDate, isPublic)
            val response = api.createItinerary(request)
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteItinerary(id: UUID): UiState<Unit> = withContext(ioDispatcher) {
        try {
            api.deleteItinerary(id)
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateVisibility(id: UUID, isPublic: Boolean): UiState<ItineraryDetail> = withContext(ioDispatcher) {
        try {
            val request = VisibilityRequest(isPublic)
            val response = api.updateVisibility(id, request)
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun addStage(itineraryId: UUID, request: CreateStageRequest): UiState<StageDetail> = withContext(ioDispatcher) {
        try {
            val response = api.addStage(itineraryId, request)
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun updateStage(
        itineraryId: UUID,
        stageId: UUID,
        request: UpdateStageRequest
    ): UiState<StageDetail> = withContext(ioDispatcher) {
        try {
            val response = api.updateStage(itineraryId, stageId, request)
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun deleteStage(itineraryId: UUID, stageId: UUID): UiState<Unit> = withContext(ioDispatcher) {
        try {
            api.deleteStage(itineraryId, stageId)
            UiState.Success(Unit)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun searchCatalog(request: CatalogSearchRequest): UiState<CatalogSearchResponse> = withContext(ioDispatcher) {
        try {
            val response = api.searchCatalog(
                query = request.query,
                category = request.category,
                location = request.location,
                page = request.page,
                size = request.size
            )
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override suspend fun getCatalogItem(id: UUID): UiState<CatalogItem> = withContext(ioDispatcher) {
        try {
            val response = api.getCatalogItem(id)
            UiState.Success(response)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun <T> handleError(e: Exception): UiState<T> {
        return when (e) {
            is HttpException -> {
                val code = e.code()
                val message = when (code) {
                    401 -> "Sessione scaduta, effettua di nuovo l'accesso"
                    403 -> "Non hai i permessi per questa operazione"
                    404 -> "Risorsa non trovata"
                    409 -> "Conflitto: la risorsa esiste già"
                    else -> "Errore del server ($code)"
                }
                val nonRetryableCodes = setOf(401, 403, 404, 409)
                UiState.Error(message, retryable = !nonRetryableCodes.contains(code))
            }
            else -> UiState.Error(e.message ?: "Errore di rete", retryable = true)
        }
    }

    private val ioDispatcher = kotlinx.coroutines.Dispatchers.IO
}