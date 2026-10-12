package com.tripflow.feature.booking.repository

import com.tripflow.core.model.UiState
import com.tripflow.core.network.catalog.dto.TripResponseDTO

interface CatalogoRepository {

    suspend fun trovaViaggio(id: String): UiState<TripResponseDTO>
}
