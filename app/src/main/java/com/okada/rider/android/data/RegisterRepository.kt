package com.okada.rider.android.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.data.model.UserInfo
import com.okada.rider.android.services.AccountService
import com.okada.rider.android.services.DataService
import kotlin.Result

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class RegisterRepository(val accountService: AccountService, val dataService: DataService) {

    // in-memory cache of the loggedInUser object
    private var loggedInUser: LoggedInUser? = null
    private var profileExists: Boolean = false

    val isLoggedIn: Boolean
        get() = loggedInUser != null

    val isProfileExists: Boolean
        get() = profileExists

    fun fetchEmailAddress(completion: (Result<LoggedInUser>) -> Unit) {
        accountService.getLoggedInUser {result ->
            result.onSuccess {user->
                loggedInUser = LoggedInUser(user.userId,
                    user.email)
                completion(Result.success(loggedInUser!!))
            }
        }
    }

    fun checkProfileExists() {
        loggedInUser?.let {
            dataService.checkIfUserInfoExists(it.userId, object : ValueEventListener {
                override fun onDataChange (dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val userInfo = dataSnapshot.getValue<UserInfo>()
                    profileExists = userInfo != null
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    profileExists = false
                }

            })
        }
    }


    fun createUserAuthentication(email: String,
                       password: String,
                       completion: (Result<Unit>) -> Unit) {
        accountService.createUser(email, password) {result ->
            result.fold(onSuccess = {user->
                loggedInUser = LoggedInUser(user.userId,
                    user.email)
                completion(Result.success(Unit))
            },onFailure = {
                completion(Result.failure(it))
            })
        }

    }

    fun createUserInfo(firstname: String,
                       lastname: String,
                       biometricId: String,
                       completion: (Result<Unit>) -> Unit) {
        accountService.getLoggedInUser {result ->
            result.fold(onSuccess = {user->
                var userInfo = UserInfo()
                userInfo.firstname = firstname
                userInfo.lastname = lastname
                userInfo.email = user.email
                userInfo.biometricId = biometricId
                dataService.createUserInfo(user.userId,userInfo,
                    failureListener = { exception ->
                    completion(Result.failure(exception))
                }, {
                    completion(Result.success(Unit))
                })

            },onFailure = {
                completion(Result.failure(it))
            })
        }

    }




}