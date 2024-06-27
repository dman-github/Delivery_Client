package com.okada.rider.android.services

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.okada.rider.android.data.model.JobInfoModel


class JobRequestServiceImpl : JobRequestService {
    private val cloudFuncRequestDriverName = "sendPN"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val jobsRef: DatabaseReference = database.getReference("jobs")
    private val jobListener: ChildEventListener? = null
    private lateinit var newJobRef: DatabaseReference
    override fun sendDriverRouteRequest(
        pickuploc: LatLng,
        driverPushToken: String,
        uid: String,
        completion: (Result<Unit>) -> Unit
    ) {
        val locstr =
            StringBuilder().append(pickuploc.latitude).append(",").append(pickuploc.longitude)
                .toString()
        val data = hashMapOf(
            "token" to driverPushToken,
            "title" to "Driver requested!",
            "body" to "This message is to check the request functionality from the Okada app",
            "clientKey" to uid,
            "pickupLoc" to locstr
        )
        val functions = FirebaseFunctions.getInstance()
        functions.getHttpsCallable(cloudFuncRequestDriverName)
            .call(data)
            .addOnSuccessListener {
                completion(Result.success(Unit))
            }
            .addOnFailureListener { exp ->
                completion(Result.failure(exp))
            }
    }

    override fun createNewJob(
        job: JobInfoModel,
        listener: ChildEventListener,
        completion: (Result<String>) -> Unit
    ) {
        // Generate a new unique job ID
        newJobRef = jobsRef.push()
        // Get the unique job ID
        val jobId = newJobRef.key
        jobId?.let { jobId ->
            newJobRef.setValue(job)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        completion(Result.success(jobId))
                        //add the listener
                        newJobRef.addChildEventListener(listener)
                    } else {
                        it.exception?.also { exception ->
                            completion(Result.failure(exception))
                        } ?: run {
                            completion(Result.failure(Exception("Cannot create Job")))
                        }
                    }
                }
        } ?: run {
            completion(Result.failure(Exception("Cannot create Job")))
        }
    }

    override  fun removeJobListener() {
        jobListener?.let{listener->
            newJobRef.removeEventListener(listener)
        }
    }
}