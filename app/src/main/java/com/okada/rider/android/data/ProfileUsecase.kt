package com.okada.rider.android.data

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.okada.rider.android.data.model.DriverInfo
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.data.model.TokenModel
import com.okada.rider.android.data.model.UserInfo
import com.okada.rider.android.services.AccountService
import com.okada.rider.android.services.DataService
import kotlin.Result

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class ProfileUsecase(val dataService: DataService) {

    private var profileExists: Boolean = false


    val isProfileExists: Boolean
        get() = profileExists

    fun checkProfileExists(user: LoggedInUser, completion: (Result<UserInfo?>) -> Unit) {

        dataService.checkIfUserInfoExists(user.userId, object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val userInfo = dataSnapshot.getValue<UserInfo>()
                profileExists = userInfo != null
                completion(Result.success(userInfo))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                completion(Result.failure(databaseError.toException()))
            }

        })
    }

    fun createUserInfo(
        firstname: String,
        lastname: String,
        biometricId: String,
        user: LoggedInUser,
        completion: (Result<Unit>) -> Unit
    ) {
        var userInfo = UserInfo()
        userInfo.firstname = firstname
        userInfo.lastname = lastname
        userInfo.email = user.email
        userInfo.biometricId = biometricId
        dataService.createUserInfo(user.userId, userInfo,
            failureListener = { exception ->
                completion(Result.failure(exception))
            }, {
                completion(Result.success(Unit))
            })

    }

    fun sendPushNotificationToken(
        uid: String,
        tokenM: TokenModel,
        completion: (Result<Unit>) -> Unit
    ) {
        dataService.updatePushMessagingToken(uid, tokenM, completion)
    }

    fun fetchDriverInfo(userId: String, completion: (Result<DriverInfo>) -> Unit) {
        dataService.fetchDriverInfo(userId, object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val driverInfo = dataSnapshot.getValue<DriverInfo>()
                driverInfo?.let {
                    completion(Result.success(driverInfo))
                } ?: run {
                    completion(Result.failure(Exception("Cannot find driver information")))
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                completion(Result.failure(databaseError.toException()))
            }

        })
    }


}