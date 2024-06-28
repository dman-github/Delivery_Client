package com.okada.rider.android.services


import com.google.firebase.database.ChildEventListener
import com.okada.rider.android.data.model.JobInfoModel
import com.okada.rider.android.data.model.Location

interface JobRequestService {

    fun sendDriverRouteRequest(
        job: JobInfoModel,
        driverPushToken: String,
        completion: (Result<Unit>) -> Unit
    )

    fun createNewJob(
        job: JobInfoModel,
        listener: ChildEventListener,
        completion: (Result<String>) -> Unit
    )

    fun removeJobListener()

}