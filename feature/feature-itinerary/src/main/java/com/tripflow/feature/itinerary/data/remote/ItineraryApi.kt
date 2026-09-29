package com.tripflow.feature.itinerary.data.remote

import com.tripflow.feature.itinerary.data.remote.dto.CatalogItemResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.CatalogSearchResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateItineraryRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.CreateStageRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.ItineraryDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.ItinerarySummaryResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.StageDetailResponseDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateStageRequestDto
import com.tripflow.feature.itinerary.data.remote.dto.UpdateVisibilityRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface ItineraryApi {

    @GET("itinerary-service/api/itineraries")
    suspend fun getMyItineraries(
        @Header("Authorization") authorization: String
    ): List<ItinerarySummaryResponseDto>

    @GET("itinerary-service/api/itineraries/{id}")
    suspend fun getItineraryDetail(
        @Path("id") id: UUID,
        @Header("Authorization") authorization: String
    ): ItineraryDetailResponseDto

    @POST("itinerary-service/api/itineraries")
    suspend fun createItinerary(
        @Header("Authorization") authorization: String,
        @Body request: CreateItineraryRequestDto
    ): ItineraryDetailResponseDto

    @DELETE("itinerary-service/api/itineraries/{id}")
    suspend fun deleteItinerary(
        @Path("id") id: UUID,
        @Header("Authorization") authorization: String
    )

    @PATCH("itinerary-service/api/itineraries/{id}/visibility")
    suspend fun updateVisibility(
        @Path("id") id: UUID,
        @Header("Authorization") authorization: String,
        @Body request: UpdateVisibilityRequestDto
    ): ItineraryDetailResponseDto

    @POST("itinerary-service/api/itineraries/{itineraryId}/stages")
    suspend fun addStage(
        @Path("itineraryId") itineraryId: UUID,
        @Header("Authorization") authorization: String,
        @Body request: CreateStageRequestDto
    ): StageDetailResponseDto

    @PUT("itinerary-service/api/itineraries/{itineraryId}/stages/{stageId}")
    suspend fun updateStage(
        @Path("itineraryId") itineraryId: UUID,
        @Path("stageId") stageId: UUID,
        @Header("Authorization") authorization: String,
        @Body request: UpdateStageRequestDto
    ): StageDetailResponseDto

    @DELETE("itinerary-service/api/itineraries/{itineraryId}/stages/{stageId}")
    suspend fun deleteStage(
        @Path("itineraryId") itineraryId: UUID,
        @Path("stageId") stageId: UUID,
        @Header("Authorization") authorization: String
    )

    @GET("itinerary-service/api/catalog/search")
    suspend fun searchCatalog(
        @Header("Authorization") authorization: String,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("location") location: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): CatalogSearchResponseDto

    @GET("itinerary-service/api/catalog/{id}")
    suspend fun getCatalogItem(
        @Path("id") id: UUID,
        @Header("Authorization") authorization: String
    ): CatalogItemResponseDto
}