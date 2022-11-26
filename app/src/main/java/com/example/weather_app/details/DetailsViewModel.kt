package com.example.weather_app.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.weather_app.database.getDatabase
import com.example.weather_app.network.WeatherProperty
import com.example.weather_app.repository.WeatherRepository
import kotlinx.coroutines.launch

enum class DeleteStatus { STARTED, ERROR, DONE }

class DetailsViewModel(weatherProperty: WeatherProperty, app: Application) : AndroidViewModel(app) {
    private val database = getDatabase(app)
    private val weatherRepository = WeatherRepository(database)

    // Expose this viewModel to the app data
    private val _selectedProperty = MutableLiveData<WeatherProperty>()
    val selectedProperty: LiveData<WeatherProperty>
        get() = _selectedProperty

    // I want to used the weatherId to delete a weather from the ddatabase
    private val _currentWeatherId = MutableLiveData<Long>()

    val currentWeatherId: LiveData<Long>
        get() = _currentWeatherId

    // The internal MutableLiveData of type DeleteStatus that stores the status of the most recent request
    private val _status = MutableLiveData<DeleteStatus>()

    // The external immutable LiveData status for the request status WeatherApiStatus
    val status: LiveData<DeleteStatus>
        get() = _status

    init {
        _selectedProperty.value = weatherProperty
        _currentWeatherId.value = weatherProperty.id
    }

    // To delete a wether using its id
    fun deleteWeather(weatherId: Long) {
        _status.value = DeleteStatus.STARTED
        // Delete started
        try {
            viewModelScope.launch {
                weatherRepository.deleteWeather(weatherId.toString())
                _status.value = DeleteStatus.DONE
                // Delete complete
            }
        } catch (e: Exception) {
            //Delete Error
            _status.value = DeleteStatus.ERROR
        }
    }
}

/**
 * Simple ViewModel factory that provides the WeatherProperty and context to the DetailViewModel.
 */
class DetailsViewModelFactory(
    private val marsProperty: WeatherProperty,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(marsProperty, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}