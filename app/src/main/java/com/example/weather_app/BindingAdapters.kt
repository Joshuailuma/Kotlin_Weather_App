package com.example.weather_app

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.weather_app.main.GridAdapter
import com.example.weather_app.main.WeatherStatus
import com.example.weather_app.network.WeatherProperty
import kotlin.math.roundToInt


// Use binding adapter to initialize PhotoGrid adapter. This Sets the recycler view data

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<WeatherProperty>?) {
    // Cast recyclerView adapter to photoGridAdapter
    val adapter = recyclerView.adapter as GridAdapter
    adapter.submitList(data)
}

@BindingAdapter("weatherLengthStatus")
fun bindStatus(statusImageView: ImageView, weatherList: List<WeatherProperty>?) {
    Log.i("Length", weatherList?.size.toString())
    if (weatherList?.size == null || weatherList.isEmpty()) {
        statusImageView.visibility = View.VISIBLE
        statusImageView.setImageResource(R.drawable.empty_box_icon)
    } else {
        statusImageView.visibility = View.GONE
    }
}

@BindingAdapter("toCelsius")
fun convertToCelsius(view: TextView, property: WeatherProperty) {
    val result = (property.temp - 273.15).roundToInt()
    // I used view to get context
    // Set the view text to the resuult and format it to have DegreeC
    view.text = view.context.getString(R.string.degree, result)
}

@BindingAdapter("weatherLoadingStatus")
fun bindLoading(progressBar: ImageView, status: WeatherStatus?) {
    when (status) {
        WeatherStatus.LOADING -> {
            progressBar.visibility = View.VISIBLE
            progressBar.setImageResource(R.drawable.loading_animation)
        }
        else -> {
            // When call is finished
            progressBar.visibility = View.GONE
        }
    }
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, icon: WeatherProperty) {
    val theIcon = icon.icon
    // The image url we will be getting the icon from
    val imgUrl = "https://openweathermap.org/img/wn/${theIcon}@4x.png"
    // The let block handles null URI
    imgUrl.let {
        // Convert url to uri. Also require to use https
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri).apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation) // Display this when its loading
                    .error(R.drawable.ic_broken_image)
            ) // Display this When there is no image
            .into(imgView)
    }
}
