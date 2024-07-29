package uk.ac.aber.dcs.chm9360.travelbuddy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.chm9360.travelbuddy.model.Destination
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.GeoDbApi
import uk.ac.aber.dcs.chm9360.travelbuddy.utils.PixabayApi

class RetrofitViewModel : ViewModel() {

    private val geoDbApi = GeoDbApi.create()
    private val pixabayApi = PixabayApi.create()

    private val _cities = MutableStateFlow<List<Destination>>(emptyList())
    val cities: StateFlow<List<Destination>> = _cities

    private val _countries = MutableStateFlow<List<Destination>>(emptyList())
    val countries: StateFlow<List<Destination>> = _countries

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _showCityList = MutableStateFlow(false)
    val showCityList: StateFlow<Boolean> = _showCityList

    private val _showCountryList = MutableStateFlow(false)
    val showCountryList: StateFlow<Boolean> = _showCountryList

    private val _imageUrls = MutableStateFlow<Map<String, String?>>(emptyMap())
    val imageUrls: StateFlow<Map<String, String?>> = _imageUrls

    private val _imageLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val imageLoadingStates: StateFlow<Map<String, Boolean>> = _imageLoadingStates

    fun searchCities(query: String) {
        if (query.length <= 2) {
            _cities.value = emptyList()
            _showCityList.value = false
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val response = geoDbApi.getCities(namePrefix = query)
                if (response.isSuccessful) {
                    val responseData = response.body()?.data
                    _cities.value = responseData ?: emptyList()
                    _showCityList.value = _cities.value.isNotEmpty()
                } else {
                    Log.e("RetrofitViewModel", "API response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("RetrofitViewModel", "API call failed: ${e.message}")
            }
            _loading.value = false
        }
    }

    fun searchCountries(query: String) {
        if (query.length <= 2) {
            _countries.value = emptyList()
            _showCountryList.value = false
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val response = geoDbApi.getCountries(namePrefix = query)
                if (response.isSuccessful) {
                    val responseData = response.body()?.data
                    _countries.value = responseData ?: emptyList()
                    _showCountryList.value = _countries.value.isNotEmpty()
                } else {
                    Log.e("RetrofitViewModel", "API response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("RetrofitViewModel", "API call failed: ${e.message}")
            }
            _loading.value = false
        }
    }

    fun hideCityList() {
        _showCityList.value = false
        _cities.value = emptyList()
    }

    fun hideCountryList() {
        _showCountryList.value = false
        _countries.value = emptyList()
    }

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
}