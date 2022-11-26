package com.example.weather_app.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weather_app.databinding.FragmentDetailsBinding
import com.google.android.material.appbar.AppBarLayout


class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var deleteStatus: DeleteStatus

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(activity).application
        binding = FragmentDetailsBinding.inflate(inflater)

        binding.lifecycleOwner = this
        val weatherProperty = DetailsFragmentArgs.fromBundle(requireArguments()).selectedWeather
        // Get details viewModelFactory
        val viewModelFactory = DetailsViewModelFactory(weatherProperty, application)
        //Get detailsViewModel
        //Use the factory to create a DetailsViewModel and bind it to the viewModel:
        detailsViewModel = ViewModelProvider(
            this, viewModelFactory
        )[DetailsViewModel::class.java]
        binding.viewModel = detailsViewModel

        coordinateMotion()
        // Inflate the layout for this fragment
        return binding.root
    }

    // When view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var weatherId: Long = 0 // initialize
        // Observe the current weatherId from our detailsViewModel
        detailsViewModel.currentWeatherId.observe(viewLifecycleOwner, Observer {
            weatherId = it
            Log.i("weatherId", weatherId.toString())
        })

        binding.deleteButton.setOnClickListener {
            // Pass the weatherId to the deleteWeather present in MainViewModel
            // We are doing the deletion in the mainViewModel because detailsVieModel do not have access to the database
            detailsViewModel.deleteWeather(weatherId)
        }

        // To observe if Fetching data from api/ coroutine suspend function is completed
        detailsViewModel.status.observe(viewLifecycleOwner, Observer {
            deleteStatus = it
            when (it) {
                // When Weather has been succesfully deleted
                DeleteStatus.DONE -> {
                    Toast.makeText(this.requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                // When loading
                DeleteStatus.STARTED -> {
                    binding.deleteButton.isEnabled = false
                    binding.deleteButton.isClickable = false
                }
                else -> {
                    // When failed to add
                    Toast.makeText(
                        this.requireContext(),
                        "Failed to delete. Try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.deleteButton.isEnabled = true
                    binding.deleteButton.isClickable = true
                }
            }
        })
    }
// For custom animation
    private fun coordinateMotion() {
        val appBarLayout: AppBarLayout = binding.appbarLayout
        val motionLayout: MotionLayout = binding.motionLayout
        val listener = AppBarLayout.OnOffsetChangedListener { unused, verticalOffset ->
            val seekPosition = -verticalOffset / appBarLayout.totalScrollRange.toFloat()
            motionLayout.progress = seekPosition
        }
        appBarLayout.addOnOffsetChangedListener(listener)
    }
}