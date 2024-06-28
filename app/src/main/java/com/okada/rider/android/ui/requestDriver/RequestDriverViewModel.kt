package com.okada.rider.android.ui.requestDriver

import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.okada.rider.android.data.DirectionsUsecase
import com.okada.rider.android.data.JobRequestUsecase
import com.okada.rider.android.data.model.DeclineRequestEvent
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.data.model.SelectedPlaceModel

class RequestDriverViewModel(
    private val directionsUsecase: DirectionsUsecase,
    private val jobRequestUsecase: JobRequestUsecase
) : ViewModel() {


    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

    private val _updateMapPolyLines = MutableLiveData<PolylineOptions>()
    val updateMapPolyLines: LiveData<PolylineOptions> = _updateMapPolyLines

    private val _updateMap = MutableLiveData<SelectedPlaceModel>()
    val updateMap: LiveData<SelectedPlaceModel> = _updateMap

    private val _triggerNearestDrivers = MutableLiveData<Boolean>()
    val triggerNearestDrivers: LiveData<Boolean> = _triggerNearestDrivers

    private val _triggerClose = MutableLiveData<Boolean>()
    val triggerClose: LiveData<Boolean> = _triggerClose

    //Model
    private val _model = RequestDriverModel()
    private lateinit var nearestDriverTimeoutHandler: Handler

    fun clearMessage() {
        _showMessage.value = null
    }

    fun setGoogleApiKey(key: String) {
        _model.apiKey = key
    }

    fun resetDeclinedDrivers() {
        _model.declinedDrivers = mutableListOf<String>()
    }

    fun stopTimeoutTimer() {
        // When the response from the driver has arrived we do not need the timeout
        nearestDriverTimeoutHandler.removeCallbacksAndMessages(null)
    }

    fun addDeclinedDriver(declinedDriver: DeclineRequestEvent) {
        _model.declinedDrivers.add(declinedDriver.driverUid)
    }

    fun viewWillStop() {
        directionsUsecase.closeConnection()
    }


    fun calculatePath(event: SelectedPlaceEvent) {
        //fetch directions between the 2 points from the Google directions api
        directionsUsecase.getDirections(
            event.originString,
            event.destString,
            _model.apiKey
        ) { result ->
            result.onSuccess { placeModel ->
                try {
                    placeModel.eventOrigin = event.origin
                    placeModel.eventDest = event.destination
                    _updateMap.value = placeModel
                } catch (e: Exception) {
                    _showMessage.value = e.message
                }
            }
            result.onFailure {
                _showMessage.value = it.message
            }
        }
    }

    fun findNearbyDriver(
        target: LatLng?,
        dest: LatLng?,
        nearestDrivers: MutableSet<DriverGeoModel>,
        userUid: String?
    ) {
        target?.let { pt ->
            if (nearestDrivers.size > 0) {
                var min = Float.MAX_VALUE
                val currentRiderLocation = Location("")
                var driverFound: DriverGeoModel? = null
                currentRiderLocation.latitude = pt.latitude
                currentRiderLocation.longitude = pt.longitude
                nearestDrivers.forEach() { driver ->
                    driver.key?.let { driverUID ->
                        // ignore the drivers that have declined
                        if (!_model.declinedDrivers.contains(driverUID)) {
                            driver.geoLocation?.let { loc ->
                                val driverLocation = Location("")
                                driverLocation.latitude = loc.latitude
                                driverLocation.longitude = loc.longitude
                                if (driverLocation.distanceTo(currentRiderLocation) < min) {
                                    min = driverLocation.distanceTo(currentRiderLocation)
                                    driverFound = driver
                                }
                            }
                        }
                    }
                }
                driverFound?.let { driver ->
                    _showMessage.value = "Found driver: ${driver.getFullName()}"
                    userUid?.let { uid ->
                        dest?.let {
                            sendDriverRequest(pt, it, driver, uid)
                        }
                    }
                } ?: run {
                    _showMessage.value = "No drivers have accepeted the job!!"
                    _triggerClose.value = true
                }
            } else {
                _showMessage.value = "Drivers not found"
            }
        }
    }

    fun sendDriverRequest(
        pickupLocation: LatLng,
        destination: LatLng,
        driver: DriverGeoModel,
        userUid: String
    ) {
        driver.key?.let { key ->
            jobRequestUsecase.createJobRequest(
                key,
                userUid,
                pickupLocation,
                destination,
                object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        _showMessage.value = "Driver request removed"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _showMessage.value = "Datebase error: $error"
                    }
                }) { result ->
                result.fold(onSuccess = {
                    // Push done
                    _showMessage.value = "push done"

                }, onFailure = {
                    // Error occurred
                    _showMessage.value = it.message
                })
                startRequestTimeoutTimer(key)
            }
        }
    }

    private fun startRequestTimeoutTimer(driverUid: String) {
        nearestDriverTimeoutHandler = Handler(Looper.getMainLooper())
        nearestDriverTimeoutHandler.postDelayed({
            _model.declinedDrivers.add(driverUid)
            _triggerNearestDrivers.value = true
        }, 15000)
    }
}

