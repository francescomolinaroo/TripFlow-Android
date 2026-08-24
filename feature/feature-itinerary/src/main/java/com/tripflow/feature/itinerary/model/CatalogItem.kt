package com.tripflow.feature.itinerary.model

import java.util.UUID

data class CatalogItem(
    val id: UUID,
    val title: String,
    val description: String,
    val location: String,
    val category: String,
    val durationMinutes: Int,
    val imageUrl: String?,
    val averageRating: Double?
)

data class CatalogSearchRequest(
    val query: String? = null,
    val category: String? = null,
    val location: String? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class CatalogSearchResponse(
    val content: List<CatalogItem>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)