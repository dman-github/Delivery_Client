package com.okada.rider.android.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.JobDetails
import com.okada.rider.android.data.model.JobInfoModel
import com.okada.rider.android.data.model.Location
import com.okada.rider.android.data.model.TokenModel
import com.okada.rider.android.data.model.enums.JobStatus
import com.okada.rider.android.services.DataService
import com.okada.rider.android.services.JobRequestService

class JobRequestUsecase(
    val jobRequestService: JobRequestService,
    val dataService: DataService
) {
    // in-memory cache of the currentJobId  object
    private var currentJobId: String? = null

    val hasActiveJob: Boolean
        get() = currentJobId != null
    fun createJobRequest(
        driverUid: String,
        userUid: String,
        pickupLocation: LatLng,
        destination: LatLng,
        listener: ValueEventListener,
        completion: (Result<Unit>) -> Unit
    ) {
        val jobRequest = JobInfoModel(
            driverUid, userUid, JobStatus.NEW, JobDetails(
                "bike",
                "some info here about the ride",
                Location(pickupLocation.latitude, pickupLocation.longitude),
                Location(destination.latitude, destination.longitude)
            )
        )
        jobRequestService.createNewJob(jobRequest, listener) { result ->
            result.fold(onSuccess = {jobId->
                // Job created send the push notification
                currentJobId = jobId
                sendDriverRouteRequest(jobId, jobRequest, completion)
            }, onFailure = {
                // Error occurred
                completion(Result.failure(it))
            })
        }

    }

    fun updateJobDriver(newDriverUid: String, completion: (Result<Unit>) -> Unit) {
        currentJobId?.let { jobId ->
            // Driver is changed to the new driver
            jobRequestService.updateJobDriver(jobId,newDriverUid) { result ->
                result.fold(onSuccess = {
                    // Job is fetched from DB
                    jobRequestService.fetchCurrentJob(jobId) {result->
                        result.fold(onSuccess = {jobInfoResult->
                            // Send the push notification request to the new driver
                            sendDriverRouteRequest(jobId,jobInfoResult, completion)
                        }, onFailure = {
                            // Error occurred
                            completion(Result.failure(it))
                        })
                    }
                }, onFailure = {
                    // Error occurred
                    completion(Result.failure(it))
                })
            }
        }
    }

    private fun sendDriverRouteRequest(
        jobId: String,
        jobRequest: JobInfoModel,
        completion: (Result<Unit>) -> Unit
    ) {
        dataService.retrievePushMessagingToken(jobRequest.driverUid!!, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {
                    completion(Result.failure(Exception("No Push token")))
                } else {
                    snapshot.getValue(TokenModel::class.java)?.let { model ->
                        jobRequestService.sendDriverRouteRequest(
                            jobId,
                            jobRequest,
                            model.token,
                        ) { result ->
                            result.fold(onSuccess = {
                                // Notification sent
                                completion(Result.success(Unit))
                            }, onFailure = {
                                // Error occurred
                                completion(Result.failure(it))
                            })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                completion(Result.failure(error.toException()))
            }
        })
    }
}