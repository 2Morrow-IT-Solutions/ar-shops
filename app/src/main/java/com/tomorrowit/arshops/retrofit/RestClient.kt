package com.tomorrowit.arshops.retrofit

import com.tomorrowit.arshops.model.NearbyStore
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * This is the preferred order, all objects are maps of Strings and Object
 * <p>
 * parameters is Params field in postman
 * headers    is Headers field in postman
 * jsonBody   is Body field in postman as Gson JsonObject
 */

interface RestClient {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/nearbyPlaces")
    fun nearbyPlaces(
        @Field("lat") latitude: Double,
        @Field("lng") longitude: Double,
        @Field("radius") radius: Int
    ): Call<List<NearbyStore>>
}