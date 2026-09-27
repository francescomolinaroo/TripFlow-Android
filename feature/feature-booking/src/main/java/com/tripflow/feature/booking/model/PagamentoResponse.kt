package com.tripflow.feature.booking.model

import java.math.BigDecimal

data class PagamentoResponse(
    val id: String,
    val importo: BigDecimal,
    val metodo: MetodoPagamento?,
    val stato: StatoPagamento,
    val ultimeQuattroCifre: String?,
    val brandCarta: String?,
    val dataPagamento: String?,
    val createdAt: String?,
    val stripePaymentIntentId: String?
)
