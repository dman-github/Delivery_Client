package com.okada.rider.android.ui.home

import android.content.Context
import android.location.Location
import android.util.Log
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
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.LocationUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.GeoQueryModel
import com.okada.rider.android.data.model.MarkerModel

class HomeViewModel(
    private val accountUsecase: AccountUsecase,
    private val locationUsecase: LocationUsecase,
    private val profileUsecase: ProfileUsecase
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
                    Log.i("App_Info", "loadAvailableDrivers")
                    // loadAvailableDrivers(currentLoc)
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
                it.onFailure { _showSnackbarMessage.value = it.message }
            },
            object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    Log.i("App_Info", "GeoQueryEventListener, key Entered $key Thread : ${Thread.currentThread().name}")
                    _model.nearestDrivers.add(DriverGeoModel(key, location))
                }

                override fun onKeyExited(key: String?) {
                    key?.let {
                        Log.i("App_Info", "GeoQueryEventListener, key Exit Thread: ${Thread.currentThread().name}\"")
                        _model.nearestDrivers.removeIf { model -> model.key == it }
                        val markerToRemove = _model.mapMarkers[it]
                        markerToRemove?.let { marker ->
                            _removeMarker.value = marker
                            _model.mapMarkers.remove(it)
                        }

                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {}

                override fun onGeoQueryReady() {
                    if (_model.distance < _model.range_limit) {
                        _model.distance++
                        loadAvailableDrivers(location, context)
                        Log.i(
                            "App_Info",
                            "Inc Distance + loadAvailableDrivers  + ${_model.distance}"
                        )
                    } else {
                        _model.distance = 10.0
                        addDriverMarkers()
                        Log.i("App_Info", "Clear Distance + addDriverMarker")
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
                    Log.i("App_Info", "Child Listener onChildAdded")
                    val geoQueryModel = snapshot.getValue(GeoQueryModel::class.java)
                    geoQueryModel?.let { geoQueryModel ->
                        geoQueryModel.l?.let { l ->
                            val geoLocation = GeoLocation(l[0], l[1])
                            val driverGeoModel =
                                DriverGeoModel(snapshot.key, geoLocation)
                            val newDriverLocation = Location("")
                            newDriverLocation.latitude = geoLocation.latitude
                            newDriverLocation.longitude = geoLocation.longitude
                            val newDist =
                                location.distanceTo(newDriverLocation) / 1000 //Kms)
                            if (newDist <= _model.range_limit) {
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


}