package com.okada.rider.android.services

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.functions.FirebaseFunctions
import com.okada.rider.android.data.model.DriverGeoModel


class DriverRequestServiceImpl : DriverRequestService {
    val cloudFuncRequestDriverName = "sendPN"
    override fun sendDriverRouteRequest(
        pickuploc: LatLng,
        driverPushToken: String,
        completion: (Result<Unit>) -> Unit
    ) {
        val locstr = StringBuilder().append(pickuploc.latitude).append(",").append(pickuploc.longitude).toString()
        val data = hashMapOf(
            "token" to driverPushToken,
            "title" to "Driver requested",
            "body" to "This message is to check the request functionality from the Okada app",
            "pickupLoc" to locstr
        )
        val functions = FirebaseFunctions.getInstance()
        functions.getHttpsCallable(cloudFuncRequestDriverName)
            .call(data)
            .addOnSuccessListener {
                completion(Result.success(Unit))
            }
            .addOnFailureListener {exp->
                completion(Result.failure(exp))
            }
    }
    /*
    override fun authenticate(email: String, password: String, completion: (Result<LoggedInUser>) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                completion(Result.success(LoggedInUser(it.result.user!!.uid, it.result.user!!.email!!)))
            } else {
                it.exception?.also {exception->
                    completion(Result.failure(exception))
                }?:run {
                    completion(Result.failure(Exception("Network Error")))
                }
            }
        }
    }

    override fun createUser(email: String, password: String, completion: (Result<LoggedInUser>) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                completion(Result.success(LoggedInUser(it.result.user!!.uid, it.result.user!!.email!!)))
            } else {
                it.exception?.also {exception->
                    completion(Result.failure(exception))
                }?:run {
                    completion(Result.failure(Exception("Network Error")))
                }
            }
        }
    }



    override fun isUserLoggedIn(completion: (Result<Boolean>) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.also {
            completion(Result.success(true))
        }?:run {
            completion(Result.success(false))
        }
    }

    override fun getLoggedInUser(completion: (Result<LoggedInUser>) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.also {user ->
            completion(Result.success(LoggedInUser(user.uid, user.email!!)))
        }?:run {
            completion(Result.failure(Throwable("No logged in User")))
        }
    }

    override fun getPushNotificationToken(completion: (Result<String>) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnFailureListener {e ->
                completion(Result.failure(e))
            }.addOnSuccessListener {token->
                completion(Result.success(token))
            }
    }

    override fun logout(completion: (Result<Unit>) -> Unit) {
        FirebaseAuth.getInstance().signOut()
        completion(Result.success(Unit))
    }
*/
}