package com.okada.rider.android.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.Common
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.DirectionsUsecase
import com.okada.rider.android.data.LocationUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.AnimationModel
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.GeoQueryModel
import com.okada.rider.android.data.model.MarkerModel
import com.okada.rider.android.services.DirectionsService
import com.okada.rider.android.services.remote.RetrofitClient
import io.reactivex.disposables.CompositeDisposable

class HomeViewModel(
    private val accountUsecase: AccountUsecase,
    private val locationUsecase: LocationUsecase,
    private val profileUsecase: ProfileUsecase,
    private val directionsUsecase: DirectionsUsecase
) : ViewModel() {


    private val _showSnackbarMessage = MutableLiveData<String?>()
    val showSnackbarMessage: LiveData<String?> = _showSnackbarMessage

    private val _updateMap = MutableLiveData<LatLng>()
    val updateMap: LiveData<LatLng> = _updateMap

    private val _updateMapDriver = MutableLiveData<MarkerModel>()
    val updateMapDriver: LiveData<MarkerModel> = _updateMapDriver

    private val _removeMarker = MutableLiveData<Marker>()
    val removeMarker: LiveData<Marker> = _removeMarker

    //Model
    private val _model = HomeModel()



    init {
        accountUsecase.getLoggedInUser { result ->
            result.fold(onSuccess = { user ->
                _model.uid = user.userId
                locationUsecase.setupDatabase(user.userId)
            }, onFailure = {
                _showSnackbarMessage.value = it.message
            })
        }
    }

    fun clearMessage() {
        _showSnackbarMessage.value = null
    }

    fun setGoogleApiKey(key: String) {
        _model.apiKey = key
    }

    fun viewWillStop() {
        directionsUsecase.closeConnection()
    }

    fun getAddress(): String {
        return _model.currentAddress
    }

    fun setDropAddress(latLng: LatLng?, address: String?) {
        _model.dropAddress = latLng
        _model.dropAddressStr = address
    }

    fun getDropAddress(): LatLng? {
        return _model.dropAddress
    }

    fun getDropAddressStr(): String? {
        return _model.dropAddressStr
    }

    fun setPickupAddress(latLng: LatLng?, address: String?) {
        _model.pickupAddress = latLng
        _model.pickUpAddressStr = address
    }

    fun getPickupAddress(): LatLng? {
        return _model.pickupAddress
    }
    fun getPickupAddressStr(): String? {
        return _model.pickUpAddressStr
    }

    fun useCurrentLocationForPickup() {
        _model.pickupAddress = _model.currentAddressLatLng
        _model.pickUpAddressStr = _model.currentAddress
    }

    fun addressComplete(): Boolean {
        return ((_model.dropAddress!= null) && (_model.pickupAddress!= null))
    }

    fun getNearestDriver(): MutableSet<DriverGeoModel> {
        return _model.nearestDrivers
    }

    fun getUserUiD(): String? {
        return _model.uid
    }

    fun removeUserLocation() {
        _model.uid?.also { uid ->
            locationUsecase.removeLocationFor(uid)
        } ?: run {
            _showSnackbarMessage.value = "No logged in user"
        }
    }

    fun updateLocation(loc: Location?, context: Context) {

        loc?.let { location ->
            _model.uid?.also { uid ->
                locationUsecase.updateLocation(uid, location) { result ->
                    result.onSuccess {
                        _model.currentLocation = location
                        _updateMap.value = LatLng(location.latitude, location.longitude)
                        _showSnackbarMessage.value =
                            "Location updated\nLat: ${location.latitude}, Lon: ${location.longitude}}"
                        loadAvailableDrivers(location, context)
                        fetchAddress(LatLng(location.latitude, location.longitude))
                    }
                    result.onFailure {
                        _showSnackbarMessage.value = it.message
                    }
                }
            } ?: run {
                _showSnackbarMessage.value = "No logged in user"
            }
        }
    }

    private fun addDriverMarkers() {
        if (_model.nearestDrivers.size <= 0) {
            _showSnackbarMessage.value = "No drivers found"
        }
        for (driver in _model.nearestDrivers) {
            fetchDriverInfoByKey(driver)
        }
    }

    fun saveMapMarker(uid: String, marker: Marker) {
        if (!_model.mapMarkers.containsKey(uid)) {
            _model.mapMarkers.put(uid, marker)
        }
    }

    fun fetchDriverInfoByKey(driverGeoModel: DriverGeoModel) {
        driverGeoModel.key?.let { uid ->
            driverGeoModel.geoLocation?.let { location ->
                profileUsecase.fetchDriverInfo(uid) { result ->
                    result.fold(onSuccess = { driverInfo ->
                        driverGeoModel.driverInfoModel = driverInfo
                        driverInfo.firstname?.let { fn ->
                            driverInfo.lastname?.let { ln ->
                                driverInfo.rating?.let { r ->
                                    if (!_model.mapMarkers.containsKey(uid)) {
                                        val mModel =
                                            MarkerModel(
                                                fn,
                                                ln,
                                                location.latitude,
                                                location.longitude,
                                                r,
                                                uid
                                            )
                                        _updateMapDriver.value = mModel
                                        // Add a listener to remove the driver
                                        addDriverRemoveListener(uid)
                                    }
                                }
                            }
                        }
                    }, onFailure = {
                        _showSnackbarMessage.value = it.message
                    })

                }

            }
        }
    }

    fun getListOfDrivers(atLocation: Location) {
        if (_model.firstTime) {
            _model.currentLocation = atLocation
            _model.previousLocation = atLocation
            _model.firstTime = false
        } else {
            _model.previousLocation = _model.currentLocation
            _model.currentLocation = atLocation
        }
        _model.currentLocation?.let { currentLoc ->
            _model.previousLocation?.let { previousLoc ->
                if (previousLoc.distanceTo(currentLoc) / 1000 <= _model.range_limit) {
                    Log.i("App_info", "loadAvailableDrivers")
                    // loadAvailableDrivers(currentLoc)
                }
            }
        }
    }

    private fun addDriverRemoveListener(uid: String) {
        _model.domain?.let { domain ->
            locationUsecase.addDriverListener(uid, domain, object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChildren()) {
                        _model.nearestDrivers.removeIf { model -> model.key == uid }
                        val markerToRemove = _model.mapMarkers[uid]
                        markerToRemove?.let { marker ->
                            _removeMarker.value = marker
                            _model.mapMarkers.remove(uid)
                            _model.driversSubscribed.remove(uid)
                        }
                    } else {
                        snapshot.getValue(GeoQueryModel::class.java)?.let { model ->
                            val animationModel = AnimationModel(false, model)
                            if (_model.driversSubscribed.containsKey(uid)) {
                                val marker = _model.mapMarkers[uid]
                                val oldPosition = _model.driversSubscribed.get(uid)
                                if (oldPosition!!.isRun) {
                                    // Animation in progress so we must stop it and restart with updated start location
                                    oldPosition.handler?.removeCallbacksAndMessages(null)
                                }
                                val from = StringBuilder()
                                    .append(oldPosition?.geoQueryModel?.l?.get(0))
                                    .append(",")
                                    .append(oldPosition?.geoQueryModel?.l?.get(1))
                                    .toString()

                                val to = StringBuilder()
                                    .append(animationModel?.geoQueryModel?.l?.get(0))
                                    .append(",")
                                    .append(animationModel?.geoQueryModel?.l?.get(1))
                                    .toString()

                                moveMarkerAnimation(uid, animationModel, marker, from, to)
                            } else {
                                // first location
                                _model.driversSubscribed.put(uid, animationModel)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _showSnackbarMessage.value = "Removing Driver ${error.message}"
                }

            })
        }
    }

    private fun moveMarkerAnimation(
        uid: String,
        newData: AnimationModel,
        marker: Marker?,
        from: String,
        to: String
    ) {
        if (!newData.isRun) {
            // Animation is active
            newData.isRun = true
            _model.driversSubscribed.put(uid, newData)
            //fetch directions between the 2 points from the Google directions api
            directionsUsecase.getDirections(from, to, _model.apiKey) { result ->
                result.onSuccess { model ->
                    try {
                        newData.polylineList = model.polylineList
                        // create the marker movement animation
                        newData.handler = Handler(Looper.getMainLooper())
                        newData.index = -1
                        newData.next = 1
                        val runnable = object : Runnable {
                            override fun run() {
                                newData.polylineList?.let { list ->
                                    // Takes 2 points at a time
                                    if (list.size > 1) {
                                        if (newData.index < list.size - 2) {
                                            newData.index++
                                            newData.next = newData.index + 1
                                            newData.start = list[newData.index]
                                            newData.end = list[newData.next]
                                        }
                                        newData.start?.let { start ->
                                            newData.end?.let { end ->
                                                marker?.let { marker ->
                                                    val startPosition = start
                                                    val endPosition = end
                                                    val startRotation = marker.rotation

                                                    val latLngInterpolator = LinearFixed()
                                                    val valueAnimator =
                                                        ValueAnimator.ofFloat(0f, 1f)
                                                    valueAnimator.setDuration(1000) // duration 1 second

                                                    valueAnimator.interpolator =
                                                        LinearInterpolator()
                                                    valueAnimator.addUpdateListener { animation ->
                                                        try {
                                                            val v = animation.animatedFraction
                                                            val newPosition =
                                                                latLngInterpolator.interpolate(
                                                                    v,
                                                                    startPosition,
                                                                    endPosition
                                                                )

                                                            val endRotation =
                                                                Common.getBearing(start, end)
                                                            marker.position = newPosition
                                                            marker.setAnchor(0.5f, 0.5f)
                                                            marker.rotation =
                                                                Common.computeRotationNew(
                                                                    v,
                                                                    startRotation,
                                                                    endRotation
                                                                )
                                                        } catch (ex: java.lang.Exception) {
                                                            // I don't care atm..
                                                        }
                                                    }
                                                    valueAnimator.start()
                                                }
                                            }
                                        }
                                        if (newData.index < list.size - 2) {
                                            // Keep running a new animation after 1s (the old animation lasts 1s too)
                                            newData.handler!!.postDelayed(this, 1000)
                                        } else if (newData.index < list.size - 1) {
                                            newData.isRun = false
                                            // We have finished the animating the entire route so we can try another one.
                                        }
                                    }
                                }
                            }
                        }
                        newData.handler!!.postDelayed(runnable, 0)
                    } catch (e: Exception) {
                        _showSnackbarMessage.value = e.message
                    }
                }
                result.onFailure {
                    _showSnackbarMessage.value = it.message
                }
            }
        }
    }

    fun loadAvailableDrivers(location: Location, context: Context) {
        locationUsecase.fetchNearestDrivers(
            location,
            _model.distance,
            context,
            completion = { it ->
                it.onSuccess { domain -> _model.domain = domain }
                it.onFailure { _showSnackbarMessage.value = it.message }
            },
            object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    Log.i(
                        "App_info",
                        "GeoQueryEventListener, key Entered $key Thread : ${Thread.currentThread().name}"
                    )
                    val geoModel = DriverGeoModel(key, location)
                    _model.nearestDrivers.add(geoModel)
                    fetchDriverInfoByKey(geoModel)
                }

                override fun onKeyExited(key: String?) {
                    key?.let {
                        Log.i(
                            "App_info",
                            "GeoQueryEventListener, key Exit Thread: ${Thread.currentThread().name}\""
                        )
                        _model.nearestDrivers.removeIf { model -> model.key == it }
                        val markerToRemove = _model.mapMarkers[it]
                        markerToRemove?.let { marker ->
                            _removeMarker.value = marker
                            _model.mapMarkers.remove(it)
                            _model.driversSubscribed.remove(it)
                        }

                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {}

                override fun onGeoQueryReady() {
                    if (_model.distance < _model.range_limit) {
                        _model.distance++
                        loadAvailableDrivers(location, context)
                        Log.i(
                            "App_info",
                            "Inc Distance + loadAvailableDrivers  + ${_model.distance}"
                        )
                    } else {
                        _model.distance = _model.range_limit
                        addDriverMarkers()
                        Log.i("App_info", "Clear Distance + addDriverMarker")
                    }
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    error?.let { _showSnackbarMessage.value = it.message }
                }
            }, object : ChildEventListener {

                override fun onChildAdded(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    val geoQueryModel = snapshot.getValue(GeoQueryModel::class.java)
                    geoQueryModel?.let { geoQueryModel ->
                        geoQueryModel.l?.let { l ->
                            Log.i("App_info", "Child Listener onChildAdded uid: ${snapshot.key}")
                            val geoLocation = GeoLocation(l[0], l[1])
                            val driverGeoModel =
                                DriverGeoModel(snapshot.key, geoLocation)
                            val newDriverLocation = Location("")
                            newDriverLocation.latitude = geoLocation.latitude
                            newDriverLocation.longitude = geoLocation.longitude
                            val newDist =
                                location.distanceTo(newDriverLocation) / 1000 //Kms)
                            if (newDist <= _model.range_limit) {
                                Log.i(
                                    "App_info",
                                    "Child Listener driver on map uid: ${snapshot.key}"
                                )
                                fetchDriverInfoByKey(driverGeoModel)
                            }
                        }
                    }
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                }

                override fun onCancelled(error: DatabaseError) {
                    error?.let { _showSnackbarMessage.value = it.message }
                }

            })
    }

    fun fetchAddress(current: LatLng) {
        //fetch directions between the 2 points from the Google directions api
        val at = StringBuilder()
            .append(current.latitude)
            .append(",")
            .append(current.longitude)
            .toString()
        directionsUsecase.getAddressForLocation(at, _model.apiKey) { result ->
            result.onSuccess { model ->
                try {
                    Log.i("App_info", "${model.first}  ${model.second}")
                    _model.currentAddress = model.first
                    _model.currentAddressLatLng = current
                } catch (e: Exception) {
                    _showSnackbarMessage.value = e.message
                }
            }
            result.onFailure {
                _showSnackbarMessage.value = it.message
            }
        }
    }
    fun clearDatabase() {
        _model.domain?.let {
            locationUsecase.removeAllListeners(it)
        }
    }

}