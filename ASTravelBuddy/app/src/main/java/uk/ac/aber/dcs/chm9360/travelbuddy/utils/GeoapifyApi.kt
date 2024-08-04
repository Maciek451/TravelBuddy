package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import uk.ac.aber.dcs.chm9360.travelbuddy.model.GeoapifyResponse
import uk.ac.aber.dcs.chm9360.travelbuddy.model.GeocodeResponse

interface GeoapifyApi {
    @GET("v2/places")
    suspend fun searchPlaces(
        @Query("apiKey") apiKey: String,
        @Query("text") query: String,
        @Query("categories") categories: String? = null,
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<GeoapifyResponse>

    @GET("v1/geocode/search")
    suspend fun geocodeCity(
        @Query("apiKey") apiKey: String,
        @Query("text") city: String,
        @Query("limit") limit: Int = 1
    ): Response<GeocodeResponse>

    companion object {
        private const val BASE_URL = "https://api.geoapify.com/"
        const val GEOAPIFY_API_KEY = "426a62757f5640eb88a69d12410f6132"

        fun create(): GeoapifyApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeoapifyApi::class.java)
        }
    }
}