package com.tripflow.feature.itinerary.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ItineraryDestinationsTest {

    @Test
    fun detailRoutePassesItineraryId() {
        assertEquals("detail/42", ItineraryDestinations.detailRoute("42"))
    }

    @Test
    fun addStageRoutePassesItineraryId() {
        assertEquals("add-stage/42", ItineraryDestinations.addStageRoute("42"))
    }

    @Test
    fun createRouteIsRegistered() {
        assertEquals("create", ItineraryDestinations.ROUTE_CREATE)
    }
}
