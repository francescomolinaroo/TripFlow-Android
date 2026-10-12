package com.tripflow.feature.booking.repository

object BookingRepositoryProvider {
    val catalogo: CatalogoRepository by lazy { CatalogoRepositoryImpl() }
    val recensioni: RecensioniRepository by lazy { RecensioniRepositoryImpl() }
    val repository: BookingRepository by lazy { BookingRepositoryImpl() }
}
