package com.tripflow.feature.itinerary.model

// Condivisione di un itinerario con un altro utente.
// displayName e' risolto da user-auth-service; se non disponibile si mostra il codice.
data class ItineraryShare(
    val userId: String,
    val canEdit: Boolean,
    val displayName: String? = null,
)
