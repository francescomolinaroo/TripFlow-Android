package com.tripflow.core.network.catalog

import com.tripflow.core.network.catalog.dto.ActivityRequestDTO
import com.tripflow.core.network.catalog.dto.ActivityResponseDTO
import com.tripflow.core.network.catalog.dto.TripRequestDTO
import com.tripflow.core.network.catalog.dto.TripResponseDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("catalog-service/api/v1/trips")
    suspend fun createTrip(@Body request: TripRequestDTO): TripResponseDTO

    @PUT("catalog-service/api/v1/trips/{id}")
    suspend fun updateTrip(@Path("id") id: String, @Body request: TripRequestDTO): TripResponseDTO

    @DELETE("catalog-service/api/v1/trips/{id}")
    suspend fun deleteTrip(@Path("id") id: String)

    @POST("catalog-service/api/v1/activities")
    suspend fun createActivity(@Body request: ActivityRequestDTO): ActivityResponseDTO
}
