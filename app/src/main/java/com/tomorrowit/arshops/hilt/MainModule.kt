package com.tomorrowit.arshops.hilt

import android.content.Context
import com.tomorrowit.arshops.location.GetLocationUseCase
import com.tomorrowit.arshops.location.GetNearbyPlacesUseCase
import com.tomorrowit.arshops.retrofit.RestClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module(includes = [RetrofitModule::class])
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideGetLocationUseCase(
        @ApplicationContext context: Context
    ): GetLocationUseCase {
        return GetLocationUseCase(context)
    }

    @Provides
    @Singleton
    fun provideGetNearbyPlacesUseCase(
        restClient: RestClient
    ): GetNearbyPlacesUseCase {
        return GetNearbyPlacesUseCase(restClient, Dispatchers.IO)
    }
}