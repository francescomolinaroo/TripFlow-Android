package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState

interface RecensioniRepository {

    suspend fun prenotazioniRecensite(): UiState<Set<String>>
}
