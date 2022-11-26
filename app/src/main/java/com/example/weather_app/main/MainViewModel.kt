package com.example.weather_app.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.weather_app.database.getDatabase
import com.example.weather_app.network.WeatherProperty
import com.example.weather_app.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// Status of network
enum class WeatherStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)

    private val weatherRepository = WeatherRepository(database)

    // The internal MutableLiveData WeatherApiStatus that stores the status of the most recent request
    // when fetching the weather, if its loading or done fetching
    private val _status = MutableLiveData<WeatherStatus>()

    // The external immutable LiveData status for the request status WeatherApiStatus
    val status: LiveData<WeatherStatus>
        get() = _status

    // To get livedata from properties
    private val _properties = MutableLiveData<List<WeatherProperty>>()

    val properties: LiveData<List<WeatherProperty>>
        get() = _properties

    var _cityName = MutableLiveData<String?>()
    val cityName: MutableLiveData<String?>
        get() = _cityName

    // Value for each weather property
    val _eachWeatherProperty = MutableLiveData<WeatherProperty?>()


    // To add city by city name
    fun addCityByName() {
        // When this function starts, set status to Loading
        _status.value = WeatherStatus.LOADING
        try {
            viewModelScope.launch(Dispatchers.Main) {
                //use city value to get weather of that city
                _cityName.value?.let {
                    //it is the value
                    val response = weatherRepository.getWeatherPropertyByCity(it)
                    if (response == "Success") {
                        _status.value = WeatherStatus.DONE // when successful set status to done
                    } else {
                        _status.value =
                            WeatherStatus.ERROR // If there is an error set status to error
                    }
                }
            }
        } catch (e: Exception) {
            _status.value = WeatherStatus.ERROR // If there is an error on trial set status to error
        }

    }

    // To get my city details using its lat and lon values
    fun addMyCityByLatLng(latitude: String, longitude: String) {
        _status.value = WeatherStatus.LOADING
        try {
            viewModelScope.launch(Dispatchers.Main) {
                val response = weatherRepository.getWeatherPropertyByLatLng(
                    latitude,
                    longitude
                )
                if (response == "Success") {
                    _status.value = WeatherStatus.DONE
                } else {
                    _status.value = WeatherStatus.ERROR
                }
            }
        } catch (e: Exception) {
            _status.value = WeatherStatus.ERROR
        }
    }

    var weather = weatherRepository.allWeather



// For navigating to details screen
    // We actually get the weather property from the fragment and pass it to the next screen
   // _navigateToSelectedWeather will contain a weather property e.g id, name, description, icon etc
    private val _navigateToSelectedWeather = MutableLiveData<WeatherProperty?>()

    val navigateToSelectedWeather: LiveData<WeatherProperty?>
        get() = _navigateToSelectedWeather

    // Pass an asteroid to the next screen
    fun displayWeatherDetails(weather: WeatherProperty) {
        _navigateToSelectedWeather.value = weather
    }

    // set the weather to null after the navigation is completed to prevent..
    // unwanted extra navigations:
    fun displayWeatherDetailsComplete() {
        _navigateToSelectedWeather.value = null
    }

    // When FAB is clicked
    private val _navigateToSearch = MutableLiveData<Boolean>()
    val navigateToSearch: LiveData<Boolean>
        get() = _navigateToSearch

    fun onFabClicked() {
        _navigateToSearch.value = true
    }

    fun onNavigatedToSearch() {
        _navigateToSearch.value = false
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("CLEARED", "View model cleared")
    }

    /**
     * Factory for constructing MainViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}