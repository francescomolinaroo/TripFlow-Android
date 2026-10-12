package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.core.network.ApiClient
import com.tripflow.feature.booking.api.RecensioniApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RecensioniRepositoryImpl(
    private val api: RecensioniApi = ApiClient.create(RecensioniApi::class.java)
) : RecensioniRepository {

    override suspend fun prenotazioniRecensite(): UiState<Set<String>> = withContext(Dispatchers.IO) {
        try {
            UiState.Success(api.mie().map { it.prenotazioneId }.toSet())
        } catch (e: HttpException) {
            UiState.Error("Errore del server (${e.code()})")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Errore di rete")
        }
    }
}
