package com.okada.rider.android.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.google.maps.android.PolyUtil
import com.google.maps.android.ui.IconGenerator
import com.okada.rider.android.Common
import com.okada.rider.android.R
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.databinding.ActivityRequestDriverBinding
import com.okada.rider.android.databinding.DestinationMarkerBinding
import com.okada.rider.android.databinding.OriginMarkerBinding
import com.okada.rider.android.services.DirectionsService
import com.okada.rider.android.services.remote.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class RequestDriverActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRequestDriverBinding
    private var selectedPlaceEvent: SelectedPlaceEvent? = null

    // Routes
    private val compositeDisposable = CompositeDisposable()
    private lateinit var valueAnimator: ValueAnimator
    lateinit var directionsService: DirectionsService

    /*private var blackPolyLine: Polyline? = null
    private var greyPolyLine: Polyline? = null
    private var polylineList: List<LatLng>? = null
    private var polylineOptions: PolylineOptions? = null
    private var blackPolyLineOptions: PolylineOptions? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null*/
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        valueAnimator.end()
        valueAnimator.cancel()
        compositeDisposable.dispose()
        if (EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(event: SelectedPlaceEvent) {
        selectedPlaceEvent = event
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRequestDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun init() {
        directionsService = RetrofitClient.instance!!.create(DirectionsService::class.java)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style))
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
                this,
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
            drawPath(event)
        }
    }

    private fun drawPath(event: SelectedPlaceEvent) {
        //Request API
        compositeDisposable.add(
            directionsService.getDirections(
                "driving",
                "less_driving",
                event.originString,
                event.destString,
                getString(R.string.GOOGLE_MAPS_API_KEY)
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { returnResult ->
                    Log.i(
                        "App_Info",
                        "Directions api returned"
                    )
                    try {
                        var blackPolyLine: Polyline? = null
                        var greyPolyLine: Polyline? = null
                        var polylineList: List<LatLng>? = null
                        var polylineOptions: PolylineOptions? = null
                        var blackPolyLineOptions: PolylineOptions? = null
                        //var originMarker: Marker? = null
                        //var destinationMarker: Marker? = null
                        val jsonObject = JSONObject(returnResult)
                        val errorString = jsonObject.getString("status")
                        if (errorString.isNotEmpty() && errorString.lowercase() != "ok") {
                            val msg = "Directions api: $errorString"
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                            return@subscribe
                        }
                        val jsonArray = jsonObject.getJSONArray("routes")
                        for (i in 0 until jsonArray.length()) {
                            val route = jsonArray.getJSONObject(i)
                            val poly = route.getJSONObject("overview_polyline")
                            val polyline = poly.getString("points")
                            polylineList = PolyUtil.decode(polyline)
                        }
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
                        valueAnimator = ValueAnimator.ofInt(0, 100)
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

                        val latLngBound = LatLngBounds.Builder().include(event.origin)
                            .include(event.destination)
                            .build()
                        //Add icon for origin
                        val objects = jsonArray.getJSONObject(0)
                        val legs = objects.getJSONArray("legs")
                        val legsObject = legs.getJSONObject(0)

                        val time = legsObject.getJSONObject(("duration"))
                        val duration = time.getString("text")

                        val start_address = legsObject.getString("start_address")
                        val end_address = legsObject.getString("end_address")

                         addOriginMarker(duration, start_address)
                         addDestinationMarker(end_address)

                        val cameraUpdate = CameraUpdateFactory
                            .newLatLngBounds(latLngBound, 100)
                        // moceCamera instead of animateCamera
                        mMap.moveCamera(cameraUpdate)
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom - 1))

                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
        )
    }

    private fun addOriginMarker(duration: String, startAddress: String) {
        val binding = OriginMarkerBinding.inflate(layoutInflater)
        val view = binding.root
        val txt_time = binding.textTime
        val txt_origin = binding.textOrigin

        txt_time.text = Common.formatDuration(duration)
        txt_origin.text = Common.formatAddress(startAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        selectedPlaceEvent?.let { evnt ->
            mMap.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(evnt.origin)
            )
        }
    }

    private fun addDestinationMarker(endAddress: String) {
        val binding = DestinationMarkerBinding.inflate(layoutInflater)
        val view = binding.root
        val txt_destination = binding.textDest

        txt_destination.text = Common.formatAddress(endAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        selectedPlaceEvent?.let { evnt ->
            mMap.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(evnt.destination)
            )
        }
    }

}