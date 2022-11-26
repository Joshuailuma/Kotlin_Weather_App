package com.example.weather_app.main

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.weather_app.R
import com.example.weather_app.databinding.FragmentMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(
            this,
            MainViewModel.Factory(activity.application)
        )[MainViewModel::class.java]
    }

    // declare a global variable of FusedLocationProviderClient
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude: String = ""
    private var longitude: String = ""
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
            }
        }
    }

    // set the adapter in the RecyclerView (the GridVIew.adapter in the binding object)
    // to a new GridViewAdapter
    private var gridAdapter: GridAdapter = GridAdapter(GridAdapter.OnClickListener { i ->
        // "i" is actually weather property gotten from the grid adapter
        viewModel.displayWeatherDetails(i)
        viewModel._eachWeatherProperty.value = i
    })

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding
        binding = FragmentMainBinding.inflate(inflater)

        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel
        // To activate that little menu up there
        setHasOptionsMenu(true)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.photosGrid.adapter = gridAdapter

        binding.root.findViewById<RecyclerView>(R.id.photos_grid).apply {
            adapter = gridAdapter
        }
        // Initialize get location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Navigation to details screen
        viewModel.navigateToSelectedWeather.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController()
                    .navigate(MainFragmentDirections.actionMainFragmentToDetailsFragment(it))
                viewModel.displayWeatherDetailsComplete()
            }
        })

        // Navigate to add weather screen
        viewModel.navigateToSearch.observe(viewLifecycleOwner,
            Observer<Boolean> { shouldNavigate ->
                if (shouldNavigate == true) {
                    val navController = this.findNavController()
                    navController.navigate(MainFragmentDirections.actionMainFragmentToAddNewWeather())
                    viewModel.onNavigatedToSearch()
                }
            })

        if (isPermissionGranted()) {
            Log.i("Location", "Permission granted before, getting logitude")

            getLastKnownLocation()
        } else {
            Log.i("Location", "About to request permission")

            requestPermission()
        }

        viewModel.weather.observe(viewLifecycleOwner, Observer { weather ->
            weather.apply {
                gridAdapter.submitList(this)
                if (this.size == null || this.isEmpty()) {
                    binding.emptyListImage.visibility = View.VISIBLE
                } else {
                    binding.emptyListImage.visibility = View.GONE
                }
                Log.i("Data", this.size.toString())
            }
        })
    }

    private fun getLastKnownLocation() {
        // Getting last location

        fusedLocationClient!!.lastLocation
            .addOnSuccessListener { location ->
                // If request is successful and location is not null
                if (location != null) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                   //"Location", "$latitude  ${longitude}
                } else {
                    // "Location is null"
                    // Request for permission
                    requestPermission()
                }
            }
    }


    @SuppressLint("MissingPermission")
    private fun requestPermission() {
        //"Request for permission for lower than Android Q"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            deviceLocationforLessThanQ()
        } else {
            //"Requesting permission for Android Q"
            requestPermissionForQ()
        }
    }

    // Check if permission is granted. Returns a boolean
    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if user turned on location, if not, ask him to
    private fun deviceLocationforLessThanQ(resolve: Boolean = true) {
        // "Getting Device Location since it is less than Q"
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // "Could not get location"
                    Snackbar.make(
                        requireView(),
                        "Could not get location", Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        deviceLocationforLessThanQ()
                    }.show()
                }
            } else {
                Snackbar.make(
                    requireView(),
                    "Location services must be enabled to use the app", Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    deviceLocationforLessThanQ()
                }.show()
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.Q)
    private fun requestPermissionForQ() {
        // Requesting Permission For Q and above
        // First check if permission has been granted. Retutrns a boolean
        val hasForegroundPermission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundPermission) {
            // Get the long and lat since it is already granted
            getLastKnownLocation()
        } else {
            // Doesn't have foreground. Requesting now
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )

            // If he denies it and we want to give him 1 more chance
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this.requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this.requireContext())
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        // Send the user to app settings if "OK" is pressed
                        startActivity(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.requireActivity().packageName, null),
                            ),
                        )

                    }
                    .create()
                    .show()
            }
            // Done with getting location, now go get the longitude and latitiude
            getLastKnownLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted! Do the
                    // location-related task you need to do.
                    getLastKnownLocation()

                    if (ActivityCompat.checkSelfPermission(
                            this.requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                        // Now check background location
                        requestPermissionForQ()
                    }

                } else {
                    // permission denied. Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this.requireContext(), "permission denied", Toast.LENGTH_LONG)
                        .show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this.requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.requireActivity().packageName, null),
                            ),
                        )
                    }
                }
                return
            }
        }
    }

    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 12433
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.get_my_weather -> {
                // Get the weather present in my location if lat and long values  ar present
                if (latitude.isNotEmpty()) {
                    // If it is present
                    viewModel.addMyCityByLatLng(latitude, longitude)
                } else {
                    // if it is not present but permission has been granted
                    if (isPermissionGranted()) {
                        Toast.makeText(
                            this.context,
                            "No location found, Please restart the app",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // if it is not present and permission has not been granted
                        Toast.makeText(
                            this.context,
                            "No location found, Please grant Location permissions",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}