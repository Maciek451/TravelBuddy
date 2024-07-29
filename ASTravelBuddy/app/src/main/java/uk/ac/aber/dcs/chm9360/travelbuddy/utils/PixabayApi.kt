package uk.ac.aber.dcs.chm9360.travelbuddy.utils

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import uk.ac.aber.dcs.chm9360.travelbuddy.model.PixabayResponse

interface PixabayApi {
    @GET("api/")
    suspend fun searchPhotos(
        @Query("key") apiKey: String = API_KEY,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo"
    ): Response<PixabayResponse>

    companion object {
        private const val BASE_URL = "https://pixabay.com/"
        private const val API_KEY = "45163177-ff095c4d3c55b4717524db97a"

        fun create(): PixabayApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PixabayApi::class.java)
        }
    }
}