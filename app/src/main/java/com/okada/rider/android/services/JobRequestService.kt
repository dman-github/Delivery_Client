package com.okada.rider.android.services

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.okada.rider.android.data.model.JobInfoModel

interface JobRequestService {

    fun sendDriverRouteRequest(
        pickuploc: LatLng,
        driverPushToken: String,
        uid: String,
        completion: (Result<Unit>) -> Unit
    )

    fun createNewJob(
        job: JobInfoModel,
        listener: ChildEventListener,
        completion: (Result<String>) -> Unit
    )

    fun removeJobListener()

}