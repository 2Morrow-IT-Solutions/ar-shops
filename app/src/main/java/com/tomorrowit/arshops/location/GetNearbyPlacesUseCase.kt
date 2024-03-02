package com.tomorrowit.arshops.location

import com.tomorrowit.arshops.model.Constants
import com.tomorrowit.arshops.model.NearbyStore
import com.tomorrowit.arshops.retrofit.RestClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GetNearbyPlacesUseCase(
    private val restClient: RestClient,
    private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): List<NearbyStore> = withContext(
        coroutineDispatcher
    ) {
        suspendCancellableCoroutine { continuation ->
            val call: Call<List<NearbyStore>> =
                restClient.nearbyPlaces(latitude, longitude, Constants.DEFAULT_RADIUS)

            call.enqueue(object : Callback<List<NearbyStore>> {
                override fun onResponse(
                    call: Call<List<NearbyStore>>,
                    response: Response<List<NearbyStore>>
                ) {
                    if (response.isSuccessful) {
                        val nearbyStores: List<NearbyStore> = response.body() ?: emptyList()
                        continuation.resume(nearbyStores)
                    } else {
                        continuation.resumeWithException(Exception("API request failed with code: ${response.code()}"))
                    }
                }

                override fun onFailure(call: Call<List<NearbyStore>>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })

            // Cancel the Retrofit call if the coroutine is cancelled
            continuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }
}