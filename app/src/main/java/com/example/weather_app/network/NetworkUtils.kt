package com.example.weather_app.network

import org.json.JSONObject

/** TO properly parse the json result/ getting the desired values from it
 **/
fun parseWeatherJsonResult(jsonResult: JSONObject): ArrayList<WeatherProperty> {
    val weatherList = ArrayList<WeatherProperty>()
    val weatherJsonObject = jsonResult.getJSONArray("weather")

    val id = jsonResult.getLong("id")
    val description = weatherJsonObject.getJSONObject(0).getString("main")
    val temp = jsonResult.getJSONObject("main").getDouble("temp")
    val name = jsonResult.getString("name")
    val icon = weatherJsonObject.getJSONObject(0).getString("icon")

    val weather = WeatherProperty(id, description, name, temp, icon)
    weatherList.add(weather)
    return weatherList
    }
