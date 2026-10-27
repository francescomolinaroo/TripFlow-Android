package com.tripflow.feature.itinerary.ui.itinerarydetail

import org.junit.Assert.assertEquals
import org.junit.Test

class PublicLinkTest {

    @Test
    fun `builds public endpoint url from base url and token`() {
        assertEquals(
            "http://10.0.2.2:8080/itinerary-service/api/itineraries/public/abc",
            PublicLink.forToken("abc", baseUrl = "http://10.0.2.2:8080/")
        )
    }
}
