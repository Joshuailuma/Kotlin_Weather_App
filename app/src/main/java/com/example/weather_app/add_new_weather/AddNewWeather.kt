package com.example.weather_app.add_new_weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weather_app.databinding.FragmentAddNewWeatherBinding
import com.example.weather_app.main.MainViewModel
import com.example.weather_app.main.WeatherStatus


class AddNewWeather : Fragment() {
    private lateinit var viewModel: MainViewModel
    private var cityName: String = ""
    private lateinit var weatherStatus: WeatherStatus
    private lateinit var binding: FragmentAddNewWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewWeatherBinding.inflate(inflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

// We previously stored the texfield data unto the view model. Now we are retrieving it
        // assigning it to variable we created here
        // to eventualy be passed to the main screen
        viewModel.cityName.observe(viewLifecycleOwner, Observer { city ->
            if (city != null) {
                // Make cityName to what we got from viewModel
                cityName = city
            }
        })

        // To observe if Fetching data from api/ coroutine suspend function is completed
        viewModel.status.observe(viewLifecycleOwner, Observer {
            weatherStatus = it
            when (it) {
                // When Weather has been succesfully added
                WeatherStatus.DONE -> {
                    Toast.makeText(this.requireContext(), "City added", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                // When loading
                WeatherStatus.LOADING -> {
                    binding.button.isEnabled = false
                    binding.button.isClickable = false
                }
                else -> {
                    // When failed to add
                    Toast.makeText(
                        this.requireContext(),
                        "Failed to add City. Try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.button.isEnabled = true
                    binding.button.isClickable = true
                }
            }
        })

        binding.button.setOnClickListener {
            // Check if there is text in the textField
            if (cityName.length > 1) {
                // Add the city to the viewModel variable
                viewModel.addCityByName()
            } else {
                // If no text
                Toast.makeText(this.requireContext(), "Please type in a City", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}