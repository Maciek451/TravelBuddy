package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import uk.ac.aber.dcs.chm9360.travelbuddy.model.GeoDbResponse

interface GeoDbApi {
    @GET("cities")
    suspend fun getCities(
        @Query("namePrefix") namePrefix: String,
        @Query("limit") limit: Int = 3
    ): Response<GeoDbResponse>

    @GET("countries")
    suspend fun getCountries(
        @Query("namePrefix") namePrefix: String,
        @Query("limit") limit: Int = 3
    ): Response<GeoDbResponse>

    companion object {
        private const val BASE_URL = "https://wft-geo-db.p.rapidapi.com/v1/geo/"
        private const val API_KEY = "598847272amsh5c4c0fe9fc84eacp1447b5jsn23aca1715732"
        private const val API_HOST = "wft-geo-db.p.rapidapi.com"

        fun create(): GeoDbApi {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("x-rapidapi-key", API_KEY)
                        .addHeader("x-rapidapi-host", API_HOST)
                        .build()
                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeoDbApi::class.java)
        }
    }
}