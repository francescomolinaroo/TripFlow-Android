package com.tripflow.feature.auth

enum class DashboardRole {
    TRAVELER,
    ORGANIZER,
    UNKNOWN
}

fun dashboardRole(rawRole: String): DashboardRole = when (rawRole.trim().uppercase()) {
    "TRAVELER" -> DashboardRole.TRAVELER
    "ORGANIZER" -> DashboardRole.ORGANIZER
    else -> DashboardRole.UNKNOWN
}