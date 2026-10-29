package com.tripflow.feature.catalog.data.repository

import com.tripflow.core.network.ApiClient
import com.tripflow.core.network.catalog.dto.ActivityRequestDTO
import com.tripflow.core.network.catalog.dto.ActivityResponseDTO
import com.tripflow.core.network.catalog.dto.TripRequestDTO
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

    override suspend fun getTripById(id: String): Result<TripResponseDTO> {
        return try {
            val response = ApiClient.catalogApi.getTripById(id)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun getTripsByOrganizer(organizerId: String): Result<List<TripResponseDTO>> {
        return try {
            val response = ApiClient.catalogApi.getTripsByOrganizer(organizerId)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun createTrip(request: TripRequestDTO): Result<TripResponseDTO> {
        return try {
            val response = ApiClient.catalogApi.createTrip(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateTrip(id: String, request: TripRequestDTO): Result<TripResponseDTO> {
        return try {
            val response = ApiClient.catalogApi.updateTrip(id, request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun deleteTrip(id: String): Result<Unit> {
        return try {
            ApiClient.catalogApi.deleteTrip(id)
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun createActivity(request: ActivityRequestDTO): Result<ActivityResponseDTO> {
        return try {
            val response = ApiClient.catalogApi.createActivity(request)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
