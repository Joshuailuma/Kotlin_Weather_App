package com.example.weather_app.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://api.openweathermap.org/"
const val API = "383bc0eedf0e53f145fcbb7144d8a6b5"
class ApiService {
    interface WeatherService {
        // Get weather
        // URL will be like https://api.openweathermap.org/data/2.5/weather?q=Lagos&APPID=383bc0eedf0e53f145fcbb7144d8a6b5
        @GET("data/2.5/weather")
        suspend fun getPropertyByCity(@Query(value = "q") city: String, @Query(value = "APPID") api: String): String

        // URL will be like https://api.openweathermap.org/data/2.5/weather?lat=6.34&lon=5.64&APPID=383bc0eedf0e53f145fcbb7144d8a6b5
        @GET("data/2.5/weather")
        suspend fun getPropertyByLatLng(@Query(value = "lat") lat: String, @Query(value = "lon") lon: String,
                                        @Query(value = "APPID") api: String): String
    }

    object WeatherApi {
        // Using retroFit and moshi for fetching and conversion of Json data
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        val retrofitService: WeatherService by lazy { retrofit.create(WeatherService::class.java) }
    }
}