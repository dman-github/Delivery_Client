package com.okada.rider.android.ui.home

import HomeViewModelFactory
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.utils.widget.ImageFilterButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.okada.rider.android.R
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.databinding.FragmentHomeBinding
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.greenrobot.eventbus.EventBus


class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mapFragment: SupportMapFragment // The fragment that contains the map
    private lateinit var mMap: GoogleMap // The map
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var slidingUpPanelLayout: SlidingUpPanelLayout
    private lateinit var useCurrentButton: ImageView
    private lateinit var autoCompleteSupportFragmentPickup: AutocompleteSupportFragment
    private lateinit var autoCompleteSupportFragmentDropoff: AutocompleteSupportFragment
    private lateinit var editTextPickup: EditText
    private lateinit var editTextDropoff: EditText

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(requireActivity(), HomeViewModelFactory()).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initViews()
        init()
        return root
    }


    private fun initViews() {
        slidingUpPanelLayout = binding.slidingUpPanelLayout
        useCurrentButton = binding.actionAdd
        useCurrentButton.setOnClickListener {
            useCurrentButtonPressed()
        }
        autoCompleteSupportFragmentPickup =
            childFragmentManager.findFragmentById(R.id.autocompleteFragmentPickup) as AutocompleteSupportFragment
        autoCompleteSupportFragmentDropoff =
            childFragmentManager.findFragmentById(R.id.autocompleteFragmentDropOff) as AutocompleteSupportFragment
        autoCompleteSupportFragmentPickup.setHint("Pickup location")
        autoCompleteSupportFragmentDropoff.setHint("Dropoff location")
        editTextPickup =
            autoCompleteSupportFragmentPickup.view?.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText
        editTextDropoff =
            autoCompleteSupportFragmentDropoff.view?.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) as EditText
    }


    private fun init() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), resources.getString(R.string.GOOGLE_MAPS_API_KEY))
        }
        setupAutoCompleteFragment()
        //set google map api key
        homeViewModel.setGoogleApiKey(resources.getString(R.string.GOOGLE_MAPS_API_KEY))

        // Create the observer which updates the UI.
        homeViewModel.showSnackbarMessage.observe(viewLifecycleOwner,
            Observer { newMessage ->
                newMessage?.let { message ->
                    mapFragment.view?.let {
                        Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
                    }
                }
            })

        homeViewModel.updateMap.observe(viewLifecycleOwner,
            Observer { newPos ->
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 15f));
            })

        homeViewModel.updateMapDriver.observe(viewLifecycleOwner,
            Observer { newMarker ->
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(newMarker.driverLat, newMarker.driverLong))
                        .flat(true)
                        .title(newMarker.getMarkerTitle())
                        .snippet(newMarker.getRating())
                        .rotation(90f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.okada_driver_marker_no_bg))
                )
                marker?.let { homeViewModel.saveMapMarker(newMarker.uid, it) }
            })
        homeViewModel.removeMarker.observe(viewLifecycleOwner,
            Observer { marker ->
                marker.remove()
            })

        // The google map builder
        locationRequest = LocationRequest.Builder(5000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(10f)
            .setMinUpdateIntervalMillis(3000).build()

        // Adding a location callback for the google map
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                homeViewModel.updateLocation(locationResult.lastLocation, requireContext())
            }
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
    }


    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
            fetchLastLocation()
        } else {
            Log.i("App_info", "onResume  NO permissions")
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.viewWillStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        homeViewModel.clearMessage()
        homeViewModel.clearDatabase()
    }

    override fun onDestroy() {
        super.onDestroy()
        homeViewModel.removeUserLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it, R.raw.maps_style
                )
            })
            //googleMap.setMapStyle(null)
            if (!success) {
                Log.e("App_Error", "Style parsing error")
            } else {
                Log.e("App_Success", "Map loaded!")
                setupMapWhenReady()
                fetchLastLocation()
            }

        } catch (e: Resources.NotFoundException) {
            Log.e("App_Error", e.message.toString())
        }
    }

    private fun setupMapWhenReady() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.setOnMyLocationButtonClickListener {
                fetchLastLocation()
                return@setOnMyLocationButtonClickListener true
            }
        }
    }

    private fun setupAutoCompleteFragment() {
        autoCompleteSupportFragmentPickup.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )
        autoCompleteSupportFragmentDropoff.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )
        autoCompleteSupportFragmentPickup.setCountries("UK")
        autoCompleteSupportFragmentDropoff.setCountries("UK")
        autoCompleteSupportFragmentPickup.setOnPlaceSelectedListener(object :
            PlaceSelectionListener {
            override fun onError(p0: Status) {
                mapFragment.view?.let {
                    Snackbar.make(it, "Error places", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onPlaceSelected(place: Place) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationProviderClient
                        .lastLocation
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Error: $e", Toast.LENGTH_SHORT
                            ).show();
                        }.addOnSuccessListener { lastLocation ->
                            /* val origin = LatLng(lastLocation.latitude, lastLocation.longitude)
                             val dest = place.latLng?.let {
                                 place.latLng?.let { it1 ->
                                     LatLng(it.latitude, it1.longitude)
                                 }
                             }*/
                            place.latLng?.let { place ->
                                homeViewModel.setPickupAddress(place)
                            }
                            checkRouteAddressComplete()
                        }
                }
            }
        })
        autoCompleteSupportFragmentDropoff.setOnPlaceSelectedListener(object :
            PlaceSelectionListener {
            override fun onError(p0: Status) {
                mapFragment.view?.let {
                    Snackbar.make(it, "Error places", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onPlaceSelected(place: Place) {
                place.latLng?.let { place ->
                    homeViewModel.setDropAddress(place)
                }
                /* This delayed response is needed because when the place fragment gets the new Place it is not updated on the internal EditText straight away */
                Handler(Looper.getMainLooper()).postDelayed({
                    checkRouteAddressComplete()
                }, 200)

            }
        })

    }

    private fun checkRouteAddressComplete() {
        if (editTextPickup.text.isEmpty() || editTextDropoff.text.isEmpty()) {
            return
        }
        if (homeViewModel.addressComplete()) {
            homeViewModel.getPickupAddress()?.let { pickupAdd ->
                homeViewModel.getDropAddress()?.let { dropoffAdd ->
                    findNavController().navigate(R.id.action_navigation_home_to_requestDriverFragment)
                    EventBus.getDefault()
                        .postSticky(SelectedPlaceEvent(pickupAdd, dropoffAdd))
                }
            }
        }
    }

    private fun useCurrentButtonPressed() {
        if (homeViewModel.getAddress().isNotEmpty()) {
            autoCompleteSupportFragmentPickup.setText(homeViewModel.getAddress())
            homeViewModel.useCurrentLocationForPickup()
            checkRouteAddressComplete()
        }
    }

    private fun cancelPickupButtonPressed() {
        homeViewModel.setPickupAddress(null)
    }

    private fun cancelDropoffButtonPressed() {
        homeViewModel.setDropAddress(null)
    }


    private fun fetchLastLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient
                .lastLocation
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error: $e", Toast.LENGTH_SHORT
                    ).show();
                }.addOnSuccessListener { lastLocation ->
                    homeViewModel.updateLocation(lastLocation, requireContext())
                }
        }
    }

}