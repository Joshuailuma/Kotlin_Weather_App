package com.example.weather_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weather_app.network.WeatherProperty

// Create the WeatherData database object
@Entity(tableName = "weather_table")
data class WeatherData constructor(
    @PrimaryKey
    val id: Long,
    val description: String,
    val name : String,
    val temp: Double,
    val icon: String
)

fun List<WeatherData>.asDomainModel(): List<WeatherProperty> {
    return map {
        WeatherProperty(
            id = it.id,
            description = it.description,
            name = it.name,
            temp = it.temp,
            icon = it.icon
        )
    }
}

// Add an extension function which converts from Asteroid objects to database objects:
fun List<WeatherProperty>.asDatabaseModel(): Array<WeatherData> {
    return map {
        WeatherData(
            id = it.id,
            description = it.description,
            name = it.name,
            temp = it.temp,
            icon = it.icon
        )
    }.toTypedArray()
}