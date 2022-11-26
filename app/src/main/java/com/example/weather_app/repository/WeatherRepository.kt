package com.example.weather_app.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.weather_app.database.WeatherDao
import com.example.weather_app.database.asDatabaseModel
import com.example.weather_app.database.asDomainModel
import com.example.weather_app.network.API
import com.example.weather_app.network.ApiService
import com.example.weather_app.network.WeatherProperty
import com.example.weather_app.network.parseWeatherJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
/**
* For caching of data
* */
class WeatherRepository(private val database: WeatherDao.WeatherDatabase) {
    // We want the UI to have a list of weather, not network weather or database weather
    // Transform database weather to weather.
    val allWeather: LiveData<List<WeatherProperty>> =
        Transformations.map(database.weatherDao.getWeather()) {
            it.asDomainModel()
        }

     suspend fun deleteWeather(weatherId: String) = withContext<Unit>(Dispatchers.IO) {
        database.weatherDao.deleteWeatherById(weatherId)
    }

     suspend fun getWeatherPropertyByCity(city: String): String {
         var response: String
         withContext(Dispatchers.IO) {
             response = try {
                 val weather = ApiService.WeatherApi.retrofitService.getPropertyByCity(city, API)
                 val result = parseWeatherJsonResult(JSONObject(weather))
                 database.weatherDao.insertAll(*result.asDatabaseModel())
                 "Success"
             } catch (e: Exception) {
                 e.toString()
             }
        }
         return response
    }

    suspend fun getWeatherPropertyByLatLng(latitude: String, longitude: String): String {
        var response: String
        withContext(Dispatchers.IO) {
            response = try {
                val weather = ApiService.WeatherApi.retrofitService.getPropertyByLatLng(latitude, longitude, API)
                val result = parseWeatherJsonResult(JSONObject(weather))
                database.weatherDao.insertAll(*result.asDatabaseModel())
                "Success"
            } catch (e: Exception) {
                e.toString()
            }
        }
        return response
    }
}