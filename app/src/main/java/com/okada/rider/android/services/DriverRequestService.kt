package com.okada.rider.android.services

import com.google.android.gms.maps.model.LatLng
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.LoggedInUser
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface DriverRequestService {

    fun sendDriverRouteRequest(
        pickuploc: LatLng,
        driverPushToken: String,
        uid: String,
        completion: (Result<Unit>) -> Unit)

}