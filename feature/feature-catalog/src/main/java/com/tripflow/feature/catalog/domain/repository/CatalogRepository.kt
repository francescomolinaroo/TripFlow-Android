package com.tripflow.feature.catalog.domain.repository

import com.tripflow.core.network.catalog.dto.TripResponseDTO

interface CatalogRepository {
    suspend fun searchTrips(
        destination: String? = null,
        minAvailableSpots: Int? = null,
        maxPrice: Double? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<TripResponseDTO>>

    suspend fun getTripById(id: String): Result<TripResponseDTO>
}