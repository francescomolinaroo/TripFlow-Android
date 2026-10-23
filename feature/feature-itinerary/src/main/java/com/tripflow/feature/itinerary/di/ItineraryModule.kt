package com.tripflow.feature.itinerary.di

import android.content.Context
import com.tripflow.core.auth.TokenStorage
import com.tripflow.core.network.ApiClient
import com.tripflow.feature.itinerary.data.mapper.ItineraryMapper
import com.tripflow.feature.itinerary.data.remote.ItineraryApi
import com.tripflow.feature.itinerary.data.repository.ItineraryRepositoryImpl
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ItineraryModule {

    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): TokenStorage = TokenStorage(context)

    @Provides
    @Singleton
    fun provideItineraryApi(tokenStorage: TokenStorage): ItineraryApi {
        ApiClient.tokenProvider = { tokenStorage.getAccessToken() }
        return ApiClient.create(ItineraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideItineraryRepository(
        api: ItineraryApi
    ): ItineraryRepository = ItineraryRepositoryImpl(api, ItineraryMapper)
}
