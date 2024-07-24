package com.okada.rider.android.services


import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.JobInfoModel

interface JobRequestService {

    fun sendDriverRouteRequest(
        jobId: String,
        job: JobInfoModel,
        driverPushToken: String,
        completion: (Result<Unit>) -> Unit
    )

    fun createNewJob(
        job: JobInfoModel,
        listener: ValueEventListener,
        completion: (Result<String>) -> Unit
    )

    fun updateJobDriver(
        jobId: String,
        driverUid: String,
        completion: (Result<Unit>) -> Unit
    )

    fun fetchCurrentJob(jobId: String,
                        completion: (Result<JobInfoModel>) -> Unit)

    fun removeJobListener()

}