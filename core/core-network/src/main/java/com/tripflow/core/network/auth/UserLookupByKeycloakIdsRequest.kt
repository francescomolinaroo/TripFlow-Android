package com.tripflow.core.network.auth

data class UserLookupByKeycloakIdsRequest(
    val keycloakIds: List<String>
)
