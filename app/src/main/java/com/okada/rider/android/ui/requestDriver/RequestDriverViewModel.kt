package com.okada.rider.android.ui.requestDriver

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.DirectionsUsecase
import com.okada.rider.android.data.JobRequestUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.AppLocation
import com.okada.rider.android.data.model.DeclineRequestEvent
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.DriverInfo
import com.okada.rider.android.data.model.JobDetails
import com.okada.rider.android.data.model.JobInfoModel
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.data.model.SelectedPlaceModel
import com.okada.rider.android.data.model.enums.JobStatus

class RequestDriverViewModel(
    private val directionsUsecase: DirectionsUsecase,
    private val jobRequestUsecase: JobRequestUsecase,
    private val profileUsecase: ProfileUsecase
) : ViewModel() {


    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

    private val _updateMapPolyLines = MutableLiveData<PolylineOptions>()
    val updateMapPolyLines: LiveData<PolylineOptions> = _updateMapPolyLines

    private val _updateMap = MutableLiveData<SelectedPlaceModel>()
    val updateMap: LiveData<SelectedPlaceModel> = _updateMap

    private val _updateMapForDriver = MutableLiveData<SelectedPlaceModel>()
    val updateMapForDriver: LiveData<SelectedPlaceModel> = _updateMapForDriver

    private val _updateDriverMarker = MutableLiveData<SelectedPlaceModel>()
    val updateDriverMarker: LiveData<SelectedPlaceModel> = _updateDriverMarker

    private val _triggerNearestDrivers = MutableLiveData<Boolean>()
    val triggerNearestDrivers: LiveData<Boolean> = _triggerNearestDrivers

    private val _triggerClose = MutableLiveData<Boolean>()
    val triggerClose: LiveData<Boolean> = _triggerClose

    private val _cancelJobDone = MutableLiveData<Boolean>()
    val cancelJobDone: LiveData<Boolean> = _cancelJobDone

    private val _jobCannotBeCancelled = MutableLiveData<Boolean>()
    val jobCannotBeCancelled: LiveData<Boolean> = _jobCannotBeCancelled

    private val _triggerJobAccepted = MutableLiveData<DriverInfo>()
    val triggerJobAccepted: LiveData<DriverInfo> = _triggerJobAccepted

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

    fun calculatePathDriverToPickup(event: SelectedPlaceEvent) {
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
                    if (!_model.plotDriverToPickup) {
                        // Plot the route to the pickup point only once
                        _updateMapForDriver.value = placeModel
                        _model.plotDriverToPickup = true
                    }
                    _updateDriverMarker.value = placeModel
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
        selectedJobPositions: SelectedPlaceEvent,
        nearestDrivers: MutableSet<DriverGeoModel>,
        userUid: String?
    ) {
        selectedJobPositions.origin.let { pt ->
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
                        sendDriverRequest(selectedJobPositions, driver, uid)
                    }
                } ?: run {
                    _showMessage.value = "No drivers have accepted the job!!"
                    _triggerClose.value = true
                }
            } else {
                _showMessage.value = "Drivers not found"
            }
        }
    }

    fun sendDriverRequest(
        selectedJobPositions: SelectedPlaceEvent,
        driver: DriverGeoModel,
        userUid: String
    ) {
        driver.key?.let { key ->
            if (!jobRequestUsecase.hasActiveJob) {
                // Create a new job
                jobRequestUsecase.createJobRequest(
                    key,
                    userUid,
                    selectedJobPositions,
                    object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.getValue(JobInfoModel::class.java)?.also { job ->
                                    /* if (job.status == JobStatus.DECLINED) {
                                         job.driverUid?.let {driverId->
                                             _model.declinedDrivers.add(driverId)
                                             this@RequestDriverViewModel.stopTimeoutTimer()
                                             _showMessage.value = "Request DECLINED"
                                         }
                                     }*/
                                    job.driverUid?.let { driverId ->
                                        checkJobStatus(driverId, job.status!!, job.jobDetails!!)
                                        _model.previousJobStatus = job.status!!
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _showMessage.value = "Error creating job: $error"
                        }
                    }) { result ->
                    result.fold(onSuccess = {
                        // Push done
                        _showMessage.value = "push done"

                    }, onFailure = {
                        // Error occurred
                        _showMessage.value = "Error cannot complete job: $it.message"
                    })
                }
            } else {
                jobRequestUsecase.updateJobDriver(key) { result ->
                    // Update driver
                    result.fold(onSuccess = {
                        // Push done
                        _showMessage.value = "push done (new) driver"
                    }, onFailure = {
                        // Error occurred
                        _showMessage.value = "Error updating job: $it.message"
                    })
                }
            }
            startRequestTimeoutTimer(key)
        }
    }

    fun cancelActiveJob() {
        if (jobRequestUsecase.hasActiveJob) {
            jobRequestUsecase.cancelJobRequest { result ->
                result.fold(onSuccess = {
                    _cancelJobDone.value = true
                }, onFailure = {
                    // Error occurred
                    _showMessage.value = "Error cancelling job: $it.message"
                })
            }
        }
    }

    fun clearDatabase() {
        jobRequestUsecase.removeJobListeners()
    }

    private fun startRequestTimeoutTimer(driverUid: String) {
        nearestDriverTimeoutHandler = Handler(Looper.getMainLooper())
        nearestDriverTimeoutHandler.postDelayed({
            _model.declinedDrivers.add(driverUid)
            _triggerNearestDrivers.value = true
        }, 30000)
    }

    private fun stopTimeoutTimer() {
        // When the response from the driver has arrived we do not need the timeout
        nearestDriverTimeoutHandler.removeCallbacksAndMessages(null)
    }

    private fun checkJobStatus(driverUid: String, jobStatus: JobStatus, jobDetails: JobDetails) {
        Log.i("App_Info", "Check job status: $jobStatus")
        when (jobStatus) {
            JobStatus.DECLINED -> {
                _model.declinedDrivers.add(driverUid)
                this@RequestDriverViewModel.stopTimeoutTimer()
                _showMessage.value = "Request DECLINED"
                _triggerNearestDrivers.value = true
            }

            JobStatus.ACCEPTED -> {
                if (_model.previousJobStatus != JobStatus.ACCEPTED) {
                    // We need to load the driver info
                    _model.plotDriverToPickup = false
                    profileUsecase.fetchDriverInfo(driverUid) { result ->
                        result.fold(onSuccess = { dInfo ->
                            this@RequestDriverViewModel.stopTimeoutTimer()
                            _showMessage.value = "Request ACCEPTED"
                            _triggerJobAccepted.value = dInfo
                        }, onFailure = {
                            // Error occurred
                            _showMessage.value = "Error fetching driver info: $it.message"
                        })
                    }
                }
                checkDriverLocation(jobDetails, JobStatus.ACCEPTED)
            }

            JobStatus.NEW -> {}
            JobStatus.CANCELLED -> {}
            JobStatus.IN_PROGRESS -> {
                if (_model.previousJobStatus != JobStatus.IN_PROGRESS) {
                    _model.plotDriverToPickup = false
                }
                _jobCannotBeCancelled.value = true
                checkDriverLocation(jobDetails, JobStatus.IN_PROGRESS)
            }

            JobStatus.COMPLETED -> {}
        }
    }

    private fun checkDriverLocation(jobDetails: JobDetails?, jobStatus: JobStatus) {
        jobDetails?.driverLocation?.let { driverLocation ->
            jobDetails.pickupLocation?.let { pickupLocation ->
                jobDetails.deliveryLocation?.let { dstLocation ->
                    if (driverLocation != _model.jobDriverCurrentLocation) {
                        _model.jobDriverCurrentLocation = driverLocation
                        var destination = LatLng(dstLocation.latitude!!, dstLocation.longitude!!)
                        if (jobStatus == JobStatus.ACCEPTED) {
                            // Driver is moving towards pickup point
                            destination =  LatLng(pickupLocation.latitude!!, pickupLocation.longitude!!)
                        }
                        Log.i("App_Info", "checkDriverLocation with status: $jobStatus")
                        calculatePathDriverToPickup(
                            SelectedPlaceEvent(
                                LatLng(driverLocation.latitude!!, driverLocation.longitude!!),
                                destination,
                                "",
                                ""
                            )
                        )
                    }
                }
            }

        }
    }
}

