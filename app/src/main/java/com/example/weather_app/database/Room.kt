package com.example.weather_app.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// implement WeatherDao to get, to store and retrieve stuff in the database
@Dao
interface WeatherDao{
    // Get all asteroids from the table according to closestApproach
    @Query("SELECT * FROM weather_table")
    fun getWeather(): LiveData<List<WeatherData>>

    @Query("DELETE FROM weather_table WHERE id = :weatherId")
     fun deleteWeatherById(weatherId: String): Int

    // Simply Replace in case of double entry
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroid: WeatherData)

    // Implement Database
    @Database(entities = [WeatherData::class], version = 1)
    abstract class WeatherDatabase : RoomDatabase() {
        abstract val weatherDao: WeatherDao
    }
}

// use the singleton pattern to get an instance of the database.
private lateinit var INSTANCE: WeatherDao.WeatherDatabase

fun getDatabase(context: Context): WeatherDao.WeatherDatabase {
    // Check whether the database has been initialized:
    // If not, initialize it
    synchronized(WeatherDao.WeatherDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                WeatherDao.WeatherDatabase::class.java,
                "weather"
            ).build()
        }
    }
    return INSTANCE
}
