package com.tripflow.feature.booking.model

import java.math.BigDecimal

data class PagamentoIntentResponse(
    val pagamentoId: String,
    val clientSecret: String,
    val importo: BigDecimal
)