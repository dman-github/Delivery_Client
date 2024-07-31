package com.okada.rider.android.ui.requestDriver

import HomeViewModelFactory
import RequestDriverViewModelFactory
import android.Manifest
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
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
import com.okada.rider.android.data.model.DeclineRequestEvent
import com.okada.rider.android.data.model.DriverInfo
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.data.model.SelectedPlaceModel
import com.okada.rider.android.databinding.ConfirmPickupMarkerBinding
import com.okada.rider.android.databinding.ConfirmPickupMarkerWithDurationBinding
import com.okada.rider.android.databinding.DestinationMarkerBinding
import com.okada.rider.android.databinding.FragmentRequestDriverBinding
import com.okada.rider.android.databinding.OriginMarkerBinding
import com.okada.rider.android.ui.home.HomeViewModel
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RequestDriverFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentRequestDriverBinding? = null
    private lateinit var requestDriverVM: RequestDriverViewModel
    private lateinit var sharedVM: HomeViewModel
    private lateinit var mapFragment: SupportMapFragment // The fragment that contains the map
    private lateinit var mMap: GoogleMap // The map
    private lateinit var fillMapsView: View
    private lateinit var btnConfirmBiker: Button
    private lateinit var btnConfirmPickup: Button
    private lateinit var confirmBikerLayout: CardView
    private lateinit var confirmPickupLayout: CardView
    private lateinit var findDriverLayout: CardView
    private lateinit var txtAddressPickup: TextView

    //Job Accepted
    private lateinit var jobAcceptedLayout: CardView
    private lateinit var txtDriverName: TextView
    private lateinit var textDriverRating: TextView
    private lateinit var img_avatar: CircleImageView

    private var selectedPlaceEvent: SelectedPlaceEvent? = null
    private var driverMarker: Marker? = null
    //  private var declineRequestEvent: DeclineRequestEvent? = null
    private lateinit var valueAnimator: ValueAnimator

    //Pulsating effect
    private var lastUserCircle: Circle? = null
    val duration = 1000
    private var lastPulseAnimator: ValueAnimator? = null


    //Spinning effect
    private val numOfSpins = 5f
    private val secondsPerOneFullRotation = 40
    private var spinAnimator: ValueAnimator? = null


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

        sharedVM =
            ViewModelProvider(
                requireActivity(),
                HomeViewModelFactory()
            ).get(HomeViewModel::class.java)


        _binding = FragmentRequestDriverBinding.inflate(inflater, container, false)
        val root: View = binding.root

        btnConfirmBiker = binding.layoutConfirmBiker.btnConfirmRoute
        btnConfirmPickup = binding.layoutConfirmPickup.btnConfirmPickup
        confirmPickupLayout = binding.layoutConfirmPickup.layoutConfirmPickup
        fillMapsView = binding.fillMaps
        confirmBikerLayout = binding.layoutConfirmBiker.layoutConfirmBiker
        findDriverLayout = binding.layoutFindingYourDriver.layoutFindingYourDriver
        txtAddressPickup = binding.layoutConfirmPickup.txtAddressPickup
        // Job accepted
        jobAcceptedLayout = binding.layoutJobDriverInfo.layoutJobAccepted
        img_avatar = binding.layoutJobDriverInfo.imgDriverAvatar
        txtDriverName = binding.layoutJobDriverInfo.textDriverName
        textDriverRating = binding.layoutJobDriverInfo.textDriverRating
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
                drawPathOfJourney(model)
            })

        requestDriverVM.triggerNearestDrivers.observe(viewLifecycleOwner,
            Observer { trigger ->
                if (trigger) {
                    selectedPlaceEvent?.let {
                        findNearByDrivers(it.origin, it.destination)
                    }
                }
            })

        requestDriverVM.triggerJobAccepted.observe(viewLifecycleOwner,
            Observer { model ->
                stopAnimations()
                driverHasAcceptedJob(model)
            })

        requestDriverVM.updateMapForDriver.observe(viewLifecycleOwner,
            Observer { placeModel ->
                drawPathOfDriver(placeModel)
            })

        requestDriverVM.triggerClose.observe(viewLifecycleOwner,
            Observer { trigger ->
                if (trigger) {
                    findNavController().popBackStack()

                }
            })

        btnConfirmBiker.setOnClickListener {
            confirmPickupLayout.visibility = View.VISIBLE
            confirmBikerLayout.visibility = View.GONE
            setDataPickup()
        }
        btnConfirmPickup.setOnClickListener {
            if (mMap == null) return@setOnClickListener
            if (selectedPlaceEvent == null) return@setOnClickListener
            // Clear map
            mMap.clear()
            //Tilt
            val cameraPos =
                CameraPosition.Builder().target(selectedPlaceEvent!!.origin).tilt(45f).zoom(16f)
                    .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))
            // Start animation
            addMarkerWithPulseMarker()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }


    override fun onStop() {
        super.onStop()
        stopAnimations()
        requestDriverVM.viewWillStop()

        if (EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)

        /*if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestEvent::class.java))
            EventBus.getDefault().removeStickyEvent(DeclineRequestEvent::class.java)*/

        EventBus.getDefault().unregister(this)
    }

    /*  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
      fun onDeclineRequestEvent(event: DeclineRequestEvent) {
          declineRequestEvent = event
          requestDriverVM.addDeclinedDriver(event)
          requestDriverVM.stopTimeoutTimer()
          selectedPlaceEvent?.let {
              findNearByDrivers(it.origin, it.destination)
          }


      }*/

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(event: SelectedPlaceEvent) {
        selectedPlaceEvent = event
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requestDriverVM.clearMessage()
    }

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

    private fun setDataPickup() {
        mMap.clear()
        addPickupMarker()
    }


    private fun setupMapWhenReady() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = false
            mMap.uiSettings.isMyLocationButtonEnabled = false
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

    private fun drawPathOfJourney(model: SelectedPlaceModel) {
        var blackPolyLine: Polyline? = null
        var greyPolyLine: Polyline? = null
        var polylineList: List<LatLng>? = null
        var polylineOptions: PolylineOptions? = null
        var blackPolyLineOptions: PolylineOptions? = null

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

        //Add Address name
        txtAddressPickup.text = if (model.startAddress != null) model.startAddress else "None"
    }

    private fun drawPathOfDriver(model: SelectedPlaceModel) {
        var polylineList: List<LatLng>? = null
        var blackPolyLineOptions: PolylineOptions? = null

        polylineList = model.polylineList
        blackPolyLineOptions = PolylineOptions()
        blackPolyLineOptions.color(Color.BLACK)
        blackPolyLineOptions.width(5f)
        blackPolyLineOptions.startCap(SquareCap())
        blackPolyLineOptions.jointType(JointType.ROUND)
        polylineList?.asIterable()?.let { iterable ->
            blackPolyLineOptions.addAll(iterable)
        }
        blackPolyLineOptions.let { options ->
            mMap.addPolyline(options)
        }


        val latLngBound = LatLngBounds.Builder().include(model.eventOrigin!!)
            .include(model.eventDest!!)
            .build()
        //Add icon for pickup
        addPickUpMarkerWithDuration(model.boundedTime!!, model.eventDest!!)
        addDriverMarker(model.eventOrigin!!)

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

        val generator = IconGenerator(requireContext())
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

    private fun addPickupMarker() {
        val binding = ConfirmPickupMarkerBinding.inflate(layoutInflater)
        val view = binding.root

        val generator = IconGenerator(requireContext())
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val icon = generator.makeIcon()
        selectedPlaceEvent?.let { evnt ->
            mMap.addMarker(
                MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(evnt.origin)
            )
        }
    }

    private fun addPickUpMarkerWithDuration(duration: String, pickupAddress: LatLng) {
        val binding = ConfirmPickupMarkerWithDurationBinding.inflate(layoutInflater)
        val view = binding.root
        val generator = IconGenerator(requireContext())
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        val textDuration = binding.textDuration
        textDuration.setText(Common.formatDurationWithoutMins(duration))
        val icon = generator.makeIcon()
        mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(pickupAddress)
        )
    }

    private fun addDriverMarker(driverLocation: LatLng) {
        driverMarker?.let {
            animateMarkerAlpha(it, 1f, 0f, 1000L) // Fade out over 1 second
            it.remove() // Remove marker after fade out
        }
        driverMarker = mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.okada_driver_marker_no_bg))
                .position(driverLocation)
                .rotation(90f)
                .flat(true)
                .anchor(0.5f, 0.5f)
        )
        driverMarker?.let { animateMarkerAlpha(it,0f,1f,1000L) }
    }

    private fun addMarkerWithPulseMarker() {
        confirmPickupLayout.visibility = View.GONE
        fillMapsView.visibility = View.VISIBLE
        findDriverLayout.visibility = View.VISIBLE

        mMap.addMarker(
            MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectedPlaceEvent!!.origin)
        )

        addPulsatingEffect(selectedPlaceEvent!!.origin)
    }

    private fun addPulsatingEffect(origin: LatLng) {
        if (lastPulseAnimator != null) lastPulseAnimator?.cancel()
        if (lastUserCircle != null) lastUserCircle?.center = origin
        lastPulseAnimator = Common.valueAnimate(duration, object : AnimatorUpdateListener {
            override fun onAnimationUpdate(p0: ValueAnimator) {
                if (lastUserCircle != null) lastUserCircle!!.radius =
                    p0!!.animatedValue.toString().toDouble() else {
                    val fColor =
                        if (Common.isDarkMode(requireContext())) R.color.md_theme_dark_tertiary else R.color.md_theme_light_tertiary
                    lastUserCircle = mMap.addCircle(
                        CircleOptions().center(origin)
                            .radius(p0!!.animatedValue.toString().toDouble())
                            .strokeColor(Color.WHITE).fillColor(fColor)
                    )
                }
            }
        })
        //start rotation camera
        startMapCameraSpinningAnimation(mMap.cameraPosition.target)
    }

    private fun startMapCameraSpinningAnimation(target: LatLng?) {
        if (spinAnimator != null) spinAnimator?.cancel()
        spinAnimator = ValueAnimator.ofFloat(0f, numOfSpins * 360)
        spinAnimator?.duration = (numOfSpins * secondsPerOneFullRotation * 1000).toLong()
        spinAnimator?.interpolator = LinearInterpolator()
        spinAnimator?.startDelay = (100)
        spinAnimator?.addUpdateListener { va ->
            val newBearingValue = va.animatedValue as Float
            target?.let { target ->
                mMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(target).zoom(16f).tilt(45f)
                            .bearing(newBearingValue).build()
                    )
                )
            }
        }
        spinAnimator?.start()
        target?.let {
            selectedPlaceEvent?.destination?.let { dest ->
                findNearByDrivers(it, dest)
            }
        }
    }

    private fun stopAnimations() {
        valueAnimator.end()
        valueAnimator.cancel()
        if (lastPulseAnimator != null) lastPulseAnimator?.end()
        if (spinAnimator != null) spinAnimator?.end()
    }

    private fun findNearByDrivers(origin: LatLng, dest: LatLng) {
        requestDriverVM.findNearbyDriver(
            origin,
            dest,
            sharedVM.getNearestDriver(),
            sharedVM.getUserUiD()
        )
    }

    private fun driverHasAcceptedJob(driver: DriverInfo) {
        mMap.clear()
        fillMapsView.visibility = View.GONE
        val cameraPos = CameraPosition.Builder().target(mMap.cameraPosition.target).tilt(0f)
            .zoom(mMap.cameraPosition.zoom).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

        if (!driver.avatar.isNullOrEmpty()) {
            Glide.with(this)
                .load(driver.avatar)
                .into(img_avatar)
        } else {
            Glide.with(this)
                .load(R.drawable.okada_logo)
                .into(img_avatar)
        }
        //Driver name
        txtDriverName.setText(driver.firstname)
        textDriverRating.setText(driver.rating.toString())

        confirmBikerLayout.visibility = View.GONE
        confirmPickupLayout.visibility = View.GONE
        jobAcceptedLayout.visibility = View.VISIBLE

    }

    private fun animateMarkerAlpha(marker: Marker, startAlpha: Float, endAlpha: Float, duration: Long) {
        val alphaAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha).apply {
            this.duration = duration
            addUpdateListener { animation ->
                marker.alpha = animation.animatedValue as Float
            }
            start()
        }
    }



}