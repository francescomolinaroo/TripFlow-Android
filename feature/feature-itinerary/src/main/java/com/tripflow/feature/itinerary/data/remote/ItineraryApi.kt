package com.tripflow.feature.itinerary.data.remote

import com.tripflow.feature.itinerary.data.remote.dto.ActivityResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.AddStopRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StopResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.TripResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateStopRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ItineraryApi {

    @GET("itinerary-service/api/itineraries")
    suspend fun getMyItineraries(): List<ItineraryResponseDto>

    @GET("itinerary-service/api/itineraries/{id}")
    suspend fun getItineraryDetail(
        @Path("id") id: String
    ): ItineraryResponseDto

    @POST("itinerary-service/api/itineraries")
    suspend fun createItinerary(
        @Body request: CreateItineraryRequestDto
    ): ItineraryResponseDto

    @DELETE("itinerary-service/api/itineraries/{id}")
    suspend fun deleteItinerary(
        @Path("id") id: String
    )

    @PATCH("itinerary-service/api/itineraries/{id}/visibility")
    suspend fun updateVisibility(
        @Path("id") id: String,
        @Body request: UpdateVisibilityRequestDto
    ): ItineraryResponseDto

    @POST("itinerary-service/api/itineraries/{itineraryId}/stops")
    suspend fun addStop(
        @Path("itineraryId") itineraryId: String,
        @Body request: AddStopRequestDto
    ): StopResponseDto

    @PUT("itinerary-service/api/itineraries/{itineraryId}/stops/{stopId}")
    suspend fun updateStop(
        @Path("itineraryId") itineraryId: String,
        @Path("stopId") stopId: String,
        @Body request: UpdateStopRequestDto
    ): StopResponseDto

    @DELETE("itinerary-service/api/itineraries/{itineraryId}/stops/{stopId}")
    suspend fun deleteStop(
        @Path("itineraryId") itineraryId: String,
        @Path("stopId") stopId: String
    )

    @GET("catalog-service/api/v1/trips/search")
    suspend fun searchCatalog(
        @Query("destination") destination: String? = null
    ): List<TripResponseDto>

    @GET("catalog-service/api/v1/activities/search")
    suspend fun searchActivities(
        @Query("name") name: String? = null
    ): List<ActivityResponseDto>

    @GET("catalog-service/api/v1/trips/{id}")
    suspend fun getCatalogItem(
        @Path("id") id: String
    ): TripResponseDto

    // --- Condivisioni (solo proprietario) ---

    @GET("itinerary-service/api/itineraries/{id}/shares")
    suspend fun getShares(
        @Path("id") id: String
    ): List<com.tripflow.feature.itinerary.data.remote.dto.ShareResponseDto>

    @POST("itinerary-service/api/itineraries/{id}/shares")
    suspend fun shareItinerary(
        @Path("id") id: String,
        @Body request: com.tripflow.feature.itinerary.data.remote.dto.ShareRequestDto
    ): com.tripflow.feature.itinerary.data.remote.dto.ShareResponseDto

    @DELETE("itinerary-service/api/itineraries/{id}/shares/{userId}")
    suspend fun removeShare(
        @Path("id") id: String,
        @Path("userId") userId: String
    )

    // Nome pubblico dell'utente con cui si condivide (user-auth-service).
    @GET("user-auth-service/api/auth/users/by-keycloak/{keycloakId}")
    suspend fun getUserByKeycloakId(
        @Path("keycloakId") keycloakId: String
    ): com.tripflow.core.network.auth.PublicUserResponse
}
