
package com.example.weather_app.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// data class to convert json to kotlin object
@Parcelize
data class WeatherProperty(
    val id: Long,
    val description: String,
    val name : String,
    val temp: Double,
    val icon: String): Parcelable // If data is Needed to be passed during navigation