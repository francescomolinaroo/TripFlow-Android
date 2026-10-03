package com.tripflow.feature.booking.repository

object BookingRepositoryProvider {
    val catalogo: CatalogoRepository by lazy { CatalogoRepositoryImpl() }
    val repository: BookingRepository by lazy { FakeBookingRepository(catalogo = catalogo) }
}
