package com.okada.rider.android.ui.requestDriver

import RequestDriverViewModelFactory
import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.google.maps.android.ui.IconGenerator
import com.okada.rider.android.Common
import com.okada.rider.android.R
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.data.model.SelectedPlaceModel
import com.okada.rider.android.databinding.DestinationMarkerBinding
import com.okada.rider.android.databinding.FragmentRequestDriverBinding
import com.okada.rider.android.databinding.OriginMarkerBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RequestDriverFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentRequestDriverBinding? = null
    private lateinit var requestDriverVM: RequestDriverViewModel
    private lateinit var mapFragment: SupportMapFragment // The fragment that contains the map
    private lateinit var mMap: GoogleMap // The map
    private var selectedPlaceEvent: SelectedPlaceEvent? = null

    // Routes
    private var blackPolyLine: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var polylineList: List<LatLng>? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolyLineOptions: PolylineOptions? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestDriverVM =
            ViewModelProvider(
                this,
                RequestDriverViewModelFactory()
            ).get(RequestDriverViewModel::class.java)

        _binding = FragmentRequestDriverBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        init()
        return root
    }

    private fun init() {
        //set google map api key
        requestDriverVM.setGoogleApiKey(resources.getString(R.string.GOOGLE_MAPS_API_KEY))
        // Create the observer which updates the UI.
        requestDriverVM.showMessage.observe(viewLifecycleOwner,
            Observer { newMessage ->
                newMessage?.let { message ->
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
                }
            })

        requestDriverVM.updateMap.observe(viewLifecycleOwner,
            Observer { model ->
                drawPath(model)
            })

        /* homeViewModel.updateMapDriver.observe(viewLifecycleOwner,
             Observer { newMarker ->
                 val moo = newMarker.getMarkerTitle()
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
             })*/

        // The google map builder
        /* locationRequest = LocationRequest.Builder(5000)
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
             LocationServices.getFusedLocationProviderClient(requireContext())*/
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    /*
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
    */
    override fun onStop() {
        super.onStop()
        requestDriverVM.viewWillStop()
        if (EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(event: SelectedPlaceEvent) {
        selectedPlaceEvent = event
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requestDriverVM.clearMessage()
    }

    /*   override fun onDestroy() {
           super.onDestroy()
           homeViewModel.removeUserLocation()
       }
   */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success =
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.maps_style
                    )
                )
            if (!success) {
                Log.e("App_Error", "Style parsing error")
            } else {
                Log.e("App_Success", "Request Driver Map loaded!")
                setupMapWhenReady()
            }

        } catch (e: Resources.NotFoundException) {
            Log.e("App_Error", e.message.toString())
        }
    }

    private fun setupMapWhenReady() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.setOnMyLocationButtonClickListener {
                selectedPlaceEvent?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.origin, 18f))
                }
                return@setOnMyLocationButtonClickListener true
            }
        }
        selectedPlaceEvent?.let { event ->
            requestDriverVM.calculatePath(event)
        }
    }

    private fun drawPath(model: SelectedPlaceModel) {
        polylineList = model.polylineList
        polylineOptions = PolylineOptions()
        polylineOptions?.color(Color.GRAY)
        polylineOptions?.width(12f)
        polylineOptions?.startCap(SquareCap())
        polylineOptions?.jointType(JointType.ROUND)
        polylineList?.asIterable()?.let { iterable ->
            polylineOptions?.addAll(iterable)
        }
        polylineOptions?.let { options ->
            greyPolyLine = mMap.addPolyline(options)
        }

        blackPolyLineOptions = PolylineOptions()
        blackPolyLineOptions?.color(Color.BLACK)
        blackPolyLineOptions?.width(5f)
        blackPolyLineOptions?.startCap(SquareCap())
        blackPolyLineOptions?.jointType(JointType.ROUND)
        polylineList?.asIterable()?.let { iterable ->
            blackPolyLineOptions?.addAll(iterable)
        }
        blackPolyLineOptions?.let { options ->
            blackPolyLine = mMap.addPolyline(options)
        }

        //Animation
        val valueAnimator = ValueAnimator.ofInt(0, 100)
        valueAnimator.duration = 1100
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { value ->
            val points = greyPolyLine!!.points
            val percentValue = value.animatedValue.toString().toInt()
            val size = points.size
            val newPoints = (size * (percentValue / 100.0f)).toInt()
            val p = points.subList(0, newPoints)
            blackPolyLine!!.points = p
        }
        valueAnimator.start()

        val latLngBound = LatLngBounds.Builder().include(model.eventOrigin!!)
            .include(model.eventDest!!)
            .build()
        //Add icon for origin
        addOriginMarker(model.boundedTime!!, model.startAddress!!)
        addDestinationMarker(model.endAddress!!)

        val cameraUpdate = CameraUpdateFactory
            .newLatLngBounds(latLngBound, 100)
        // moveCamera instead of animateCamera
        mMap.moveCamera(cameraUpdate)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom - 1))
    }
    private fun addOriginMarker(duration: String, startAddress: String) {
        val binding = OriginMarkerBinding.inflate(layoutInflater)
        val view = binding.root
        val txt_time = binding.textTime
        val txt_origin = binding.textOrigin

        txt_time.text = Common.formatDuration(duration)
        txt_origin.text = Common.formatAddress(startAddress)

        val generator = IconGenerator(requireContext())
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        selectedPlaceEvent?.let { evnt ->
            originMarker = mMap.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(evnt.origin)
            )
        }
    }

    private fun addDestinationMarker(endAddress: String) {
        val binding = DestinationMarkerBinding.inflate(layoutInflater)
        val view = binding.root
        val txt_destination = binding.textDest

        txt_destination.text = Common.formatAddress(endAddress)

        val generator = IconGenerator(requireContext())
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        selectedPlaceEvent?.let { evnt ->
            destinationMarker = mMap.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(evnt.destination)
            )
        }
    }

}