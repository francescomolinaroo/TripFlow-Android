package com.tripflow.core.network.catalog

import com.tripflow.core.network.catalog.dto.TripResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface CatalogApi {
    @GET("catalog-service/api/v1/trips/search")
    suspend fun searchTrips(
        @Query("destination") destination: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("minAvailableSpots") minAvailableSpots: Int? = null
    ): List<TripResponseDTO>

    @GET("catalog-service/api/v1/trips/{id}")
    suspend fun getTripById(@Path("id") id: String): TripResponseDTO
}