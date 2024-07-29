package com.okada.rider.android.services

import android.R.attr.name
import android.location.Location
import androidx.annotation.UiThread
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.okada.rider.android.data.model.JobInfoModel
import com.okada.rider.android.data.model.enums.JobStatus


class JobRequestServiceImpl : JobRequestService {
    private val cloudFuncRequestDriverName = "sendPN"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val jobsRef: DatabaseReference = database.getReference("Jobs")
    private val jobListener: ChildEventListener? = null
    private lateinit var newJobRef: DatabaseReference
    override fun sendDriverRouteRequest(
        jobId: String,
        job: JobInfoModel,
        driverPushToken: String,
        completion: (Result<Unit>) -> Unit
    ) {
        val locstr =
            StringBuilder().append(job.jobDetails!!.pickupLocation!!.latitude).append(",").append(job.jobDetails!!.pickupLocation!!.longitude)
                .toString()
        val data = hashMapOf(
            "token" to driverPushToken,
            "title" to "Driver requested!",
            "body" to "This message is to check the request functionality from the Okada app",
            "clientKey" to jobId,
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
        listener: ValueEventListener,
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
                        newJobRef.addValueEventListener(listener)
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

    override fun updateJobStatus(
        jobId: String,
        jobStatus: JobStatus,
        completion: (Result<Unit>) -> Unit
    ) {
        val values: MutableMap<String, Any> = HashMap()
        values["status"] = jobStatus.toString()
        jobsRef.child(jobId).updateChildren(values).addOnCompleteListener {
            if (it.isSuccessful) {
                completion(Result.success(Unit))
            } else {
                it.exception?.also { exception ->
                    completion(Result.failure(exception))
                } ?: run {
                    completion(Result.failure(Exception("Cannot modify Job")))
                }
            }
        }
    }

    override fun updateJobDriver(
        jobId: String,
        driverUid: String,
        completion: (Result<Unit>) -> Unit
    ) {
        val values: MutableMap<String, Any?> = HashMap()
        values["driverUid"] = driverUid
        // A new driver resets the state to NEW
        values["status"] = JobStatus.NEW.toString()
        // Clear the new driver location fields
        values["JobDetails/driverLocation"] = null
        jobsRef.child(jobId).updateChildren(values).addOnCompleteListener{
            if(it.isSuccessful){
                completion(Result.success(Unit))
            } else {
                it.exception?.also { exception ->
                    completion(Result.failure(exception))
                } ?: run {
                    completion(Result.failure(Exception("Cannot update Job")))
                }
            }
        }}

    override fun fetchCurrentJob(jobId: String,
                                 completion: (Result<JobInfoModel>) -> Unit) {
        jobsRef.child(jobId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    snapshot.getValue(JobInfoModel::class.java)?.also { job ->
                        completion(Result.success(job))
                    } ?: run {
                        completion(Result.failure(Exception("Cannot fetch Job")))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                completion(Result.failure(Exception("Cannot fetch Job")))
            }
        })
    }

    override  fun removeJobListener() {
        jobListener?.let{listener->
            newJobRef.removeEventListener(listener)
        }
    }
}