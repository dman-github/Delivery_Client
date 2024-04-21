package com.okada.rider.android.ui.home

import HomeViewModelFactory
import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.okada.rider.android.R
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.databinding.FragmentHomeBinding

interface FirebaseDriverInfoListener {
    fun onDriverInfoLoadSuccess(driverGeoModel: DriverGeoModel?) {

    }
}

interface FirebaseDriverFailedListener {
    fun onFirebaseFailed(message: String) {

    }
}

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mapFragment: SupportMapFragment // The fragment that contains the map
    private lateinit var mMap: GoogleMap // The map
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var iFirebaseDriverInfoListener: FirebaseDriverInfoListener
    lateinit var iFirebaseDriverFailedListener: FirebaseDriverFailedListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this, HomeViewModelFactory()).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        init()
        return root
    }

    private fun init() {
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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f));
            })

        homeViewModel.updateMapDriver.observe(viewLifecycleOwner,
            Observer { newMarker ->
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(newMarker.driverLat, newMarker.driverLong))
                        .flat(true)
                        .title(newMarker.getMarkerTitle())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
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
            Log.i("App_Info", "onResume  NO permissions")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        homeViewModel.clearMessage()
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