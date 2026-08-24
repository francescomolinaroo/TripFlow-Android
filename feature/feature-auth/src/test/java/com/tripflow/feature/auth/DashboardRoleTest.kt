package com.tripflow.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardRoleTest {
    @Test
    fun roleIsNormalizedBeforeRendering() {
        assertEquals(DashboardRole.ORGANIZER, dashboardRole(" organizer "))
        assertEquals(DashboardRole.TRAVELER, dashboardRole("traveler"))
    }

    @Test
    fun unknownRoleUsesSafeFallback() {
        assertEquals(DashboardRole.UNKNOWN, dashboardRole("ADMIN"))
    }
}