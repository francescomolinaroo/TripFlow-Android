package com.tripflow.feature.catalog.data.repository

import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import com.tripflow.feature.catalog.domain.repository.CatalogRepository

class CatalogRepositoryImpl : CatalogRepository {
    override suspend fun searchTrips(
        destination: String?,
        minAvailableSpots: Int?,
        maxPrice: Double?,
        startDate: String?,
        endDate: String?
    ): Result<List<TripResponseDTO>> {
        return try {
            val response = ApiClient.catalogApi.searchTrips(
                destination = destination?.takeIf { it.isNotBlank() },
                minAvailableSpots = minAvailableSpots,
                maxPrice = maxPrice,
                startDate = startDate,
                endDate = endDate
            )
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}