package com.tripflow.feature.itinerary.ui.itinerarydetail

import com.tripflow.core.network.ApiClient

// Link pubblico di un itinerario PUBLIC: punta all'endpoint
// GET /api/itineraries/public/{token}, accessibile senza login.
object PublicLink {
    private const val PUBLIC_PATH = "itinerary-service/api/itineraries/public/"

    fun forToken(token: String, baseUrl: String = ApiClient.retrofit.baseUrl().toString()): String =
        baseUrl.trimEnd('/') + "/" + PUBLIC_PATH + token
}
