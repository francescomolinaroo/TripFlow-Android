package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.catalog.CatalogApi
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class CatalogoRepositoryImpl(
    private val api: CatalogApi = ApiClient.catalogApi
) : CatalogoRepository {

    override suspend fun trovaViaggio(id: String): UiState<TripResponseDTO> = withContext(Dispatchers.IO) {
        try {
            UiState.Success(api.getTripById(id))
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> UiState.Error("Viaggio non trovato", retryable = false)
                else -> UiState.Error("Errore del server (${e.code()})")
            }
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Errore di rete")
        }
    }
}
