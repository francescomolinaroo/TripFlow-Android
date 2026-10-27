package com.tripflow.core.network.review.dto

data class ReviewResponseDTO(
    val id: String,
    val oggettoId: String,
    val autoreNome: String? = null,
    val valutazione: Int = 0,
    val commento: String? = null,
    val createdAt: String? = null,
    val titolo: String? = null,
    val viaggiatoreId: String? = null,
    val prenotazioneId: String? = null,
    val tipoOggetto: String? = null,
    val oggettoNome: String? = null,
    val updatedAt: String? = null
) {
    // Compatibilità e helper per codice esistente
    val tripId: String get() = oggettoId
    val reviewerName: String get() = autoreNome ?: "Anonimo"
    val rating: Int get() = valutazione
    val comment: String get() = commento ?: ""
}