package uk.ac.aber.dcs.chm9360.travelbuddy.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.Feature
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.GeoapifyApi
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.PixabayApi

class RetrofitViewModel : ViewModel() {

    private val pixabayApi = PixabayApi.create()
    private val geoapifyApi = GeoapifyApi.create()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _imageUrls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val imageUrls: StateFlow<Map<String, String?>> = _imageUrls

    private val _imageLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val imageLoadingStates: StateFlow<Map<String, Boolean>> = _imageLoadingStates

    private val _places = MutableStateFlow<List<Feature>>(emptyList())
    val places: StateFlow<List<Feature>> = _places

    private val _autocompleteSuggestions = MutableStateFlow<List<String>>(emptyList())
    val autocompleteSuggestions: StateFlow<List<String>> = _autocompleteSuggestions

    fun fetchImage(destination: String) {
        viewModelScope.launch {
            _imageLoadingStates.value += (destination to true)
            try {
                val response = pixabayApi.searchPhotos(query = destination)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.hits?.isNotEmpty() == true) {
                        _imageUrls.value += (destination to body.hits.first().largeImageURL)
                    } else {
                        _imageUrls.value += (destination to null)
                        Log.d("RetrofitViewModel", "No images found for destination: $destination")
                    }
                } else {
                    _imageUrls.value += (destination to null)
                    Log.e("RetrofitViewModel", "API response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _imageUrls.value += (destination to null)
                Log.e("RetrofitViewModel", "Image fetching failed: ${e.message}")
            }
            _imageLoadingStates.value += (destination to false)
        }
    }

    fun searchPlaces(city: String, categories: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val boundingBox = getBoundingBoxForCity(city)
                val filter = if (boundingBox != null) {
                    "rect:${boundingBox.joinToString(",")}"
                } else {
                    null
                }

                val response = geoapifyApi.searchPlaces(
                    apiKey = GeoapifyApi.GEOAPIFY_API_KEY,
                    query = city,
                    categories = categories,
                    filter = filter
                )
                if (response.isSuccessful) {
                    val responseData = response.body()?.features
                    _places.value = responseData ?: emptyList()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RetrofitViewModel", "Geoapify response not successful: ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("RetrofitViewModel", "Geoapify call failed: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun getBoundingBoxForCity(city: String): List<Double>? {
        return try {
            val response = geoapifyApi.geocodeCity(
                apiKey = GeoapifyApi.GEOAPIFY_API_KEY,
                city = city
            )
            if (response.isSuccessful) {
                response.body()?.features?.firstOrNull()?.bbox
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("RetrofitViewModel", "Geoapify response not successful: ${response.code()}: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("RetrofitViewModel", "Geoapify call failed: ${e.message}")
            null
        }
    }

    fun fetchAutocompleteSuggestions(query: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = geoapifyApi.geocodeCity(
                    apiKey = GeoapifyApi.GEOAPIFY_API_KEY,
                    city = query,
                    limit = 5
                )
                if (response.isSuccessful) {
                    val suggestions = response.body()?.features?.map { it.properties.formatted } ?: emptyList()
                    _autocompleteSuggestions.value = suggestions
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RetrofitViewModel", "Geoapify response not successful: ${response.code()}: $errorBody")
                    _autocompleteSuggestions.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("RetrofitViewModel", "Geoapify call failed: ${e.message}")
                _autocompleteSuggestions.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}