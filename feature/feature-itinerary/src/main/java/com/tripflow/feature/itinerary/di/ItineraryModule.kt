package com.tripflow.feature.itinerary.di

import android.content.Context
import com.tripflow.core.auth.TokenStorage
import com.tripflow.feature.itinerary.data.mapper.ItineraryMapper
import com.tripflow.feature.itinerary.data.remote.ItineraryApi
import com.tripflow.feature.itinerary.data.repository.ItineraryRepositoryImpl
import com.tripflow.feature.itinerary.repository.ItineraryRepository
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ItineraryModule {

    @Provides
    @Singleton
    fun provideItineraryApi(): ItineraryApi {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItineraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthTokenProvider(@ApplicationContext context: Context): () -> String {
        val tokenStorage = TokenStorage(context)
        return { tokenStorage.getAccessToken() ?: "" }
    }

    @Provides
    @Singleton
    fun provideItineraryRepository(
        api: ItineraryApi,
        authTokenProvider: () -> String,
        mapper: ItineraryMapper = ItineraryMapper
    ): ItineraryRepository {
        return ItineraryRepositoryImpl(api, authTokenProvider, mapper)
    }
}