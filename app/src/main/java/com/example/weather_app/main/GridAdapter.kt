
package com.example.weather_app.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather_app.databinding.GridViewItemBinding
import com.example.weather_app.network.WeatherProperty

// Bind our recycler view to this adapter

// In GridAdapter.kt create a 'GridAdapter' class that extends a..
// RecyclerView ListAdapter with DiffCallback. Have it use a custom..
// GridAdapter.WeatherPropertyViewHolder to present a list of <WeatherProperty> objects

class GridAdapter (val onClickListener: OnClickListener) : ListAdapter<WeatherProperty, GridAdapter.WeatherPropertyViewHolder>(
    DiffCallback
) {
    // Create inner view holderclass
    // Use binding variable to bind Weather properties to the layout
    class WeatherPropertyViewHolder(private var binding: GridViewItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        // A method that takes Weather properties and sets it in the binding class
        fun bind(weatherProperty: WeatherProperty) {
            binding.property = weatherProperty
            binding.executePendingBindings()
        }
    }
// Create the DiffCallback companion object and override its two..
// required areItemsTheSame() methods:
// We want to compare two WeatherProperty

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherProperty>() {
        override fun areItemsTheSame(oldItem: WeatherProperty, newItem: WeatherProperty): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: WeatherProperty, newItem: WeatherProperty): Boolean {
            // Use standard equality operator
            return oldItem.id == newItem.id
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            WeatherPropertyViewHolder {
        // Needs to return this after inflation
        return WeatherPropertyViewHolder(GridViewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: WeatherPropertyViewHolder, position: Int) {
        val weatherProperty = getItem(position)
        // Call on click listener here passing in the weatherProperty
        holder.itemView.setOnClickListener {
            onClickListener.onClick(weatherProperty)
        }
        holder.bind(weatherProperty)
    }

    // create an internal OnClickListener class with a lambda in its constructor that..
// initializes a matching onClick function:
    class OnClickListener(val clickListener: (weatherProperty: WeatherProperty) -> Unit) {
        fun onClick(weatherProperty: WeatherProperty) = clickListener(weatherProperty)
    }
}
