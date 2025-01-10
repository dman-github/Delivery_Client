package com.okada.rider.android.ui.requestDriver

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.okada.rider.android.data.model.AppLocation
import com.okada.rider.android.data.model.enums.JobStatus
import java.time.Duration
import java.time.LocalDateTime
import java.util.Date

class RequestDriverModel {
    var apiKey: String = ""
    var declinedDrivers =  mutableListOf<String>()
    var timedOutDrivers =  mutableListOf<String>()
    var jobDriverCurrentLocation: AppLocation = AppLocation(0.0, 0.0)
    var previousJobStatus = JobStatus.NEW
   // var jobAcceptedRx = false;
    var plotDriverToPickup = false
    var plotDriverToDest = false
    private val requestCycleLimit = 3;
    private val requestCycleDurationLimit = 30;
    private var requestCycleCount = 0;
    private var requestCycleStartTime = LocalDateTime.now()


    fun resetRequestCycleParams() {
        requestCycleCount = 0;
        requestCycleStartTime = LocalDateTime.now()
    }

    fun shouldRetryRequest() : Boolean {
        requestCycleCount++
        val duration = (Duration.between(LocalDateTime.now(), requestCycleStartTime).abs()).seconds
        Log.i("App_Info", "shouldRetryRequest: $requestCycleCount, $duration ")
        return requestCycleCount <= requestCycleLimit
                && duration <= requestCycleDurationLimit
    }
}