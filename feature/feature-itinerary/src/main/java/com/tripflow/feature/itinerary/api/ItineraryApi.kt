package com.tripflow.feature.itinerary.api

import com.tripflow.feature.itinerary.model.CatalogItem
import com.tripflow.feature.itinerary.model.CatalogSearchRequest
import com.tripflow.feature.itinerary.model.CatalogSearchResponse
import com.tripflow.feature.itinerary.model.CreateStageRequest
import com.tripflow.feature.itinerary.model.ItineraryDetail
import com.tripflow.feature.itinerary.model.ItinerarySummary
import com.tripflow.feature.itinerary.model.StageDetail
import com.tripflow.feature.itinerary.model.UpdateStageRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface ItineraryApi {

    @GET("itinerary-service/api/itineraries")
    suspend fun getMyItineraries(): List<ItinerarySummary>

    @GET("itinerary-service/api/itineraries/{id}")
    suspend fun getItineraryDetail(@Path("id") id: UUID): ItineraryDetail

    @POST("itinerary-service/api/itineraries")
    suspend fun createItinerary(@Body request: CreateItineraryRequest): ItineraryDetail

    @DELETE("itinerary-service/api/itineraries/{id}")
    suspend fun deleteItinerary(@Path("id") id: UUID)

    @PATCH("itinerary-service/api/itineraries/{id}/visibility")
    suspend fun updateVisibility(@Path("id") id: UUID, @Body request: VisibilityRequest): ItineraryDetail

    @POST("itinerary-service/api/itineraries/{itineraryId}/stages")
    suspend fun addStage(@Path("itineraryId") itineraryId: UUID, @Body request: CreateStageRequest): StageDetail

    @PUT("itinerary-service/api/itineraries/{itineraryId}/stages/{stageId}")
    suspend fun updateStage(
        @Path("itineraryId") itineraryId: UUID,
        @Path("stageId") stageId: UUID,
        @Body request: UpdateStageRequest
    ): StageDetail

    @DELETE("itinerary-service/api/itineraries/{itineraryId}/stages/{stageId}")
    suspend fun deleteStage(@Path("itineraryId") itineraryId: UUID, @Path("stageId") stageId: UUID)

    @GET("itinerary-service/api/catalog/search")
    suspend fun searchCatalog(
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("location") location: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): CatalogSearchResponse

    @GET("itinerary-service/api/catalog/{id}")
    suspend fun getCatalogItem(@Path("id") id: UUID): CatalogItem
}

data class CreateItineraryRequest(
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val isPublic: Boolean
)

data class VisibilityRequest(val isPublic: Boolean)