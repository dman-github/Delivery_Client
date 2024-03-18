package com.okada.rider.android.data

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.data.model.UserInfo
import com.okada.rider.android.services.AccountService
import com.okada.rider.android.services.DataService
import com.okada.rider.android.ui.login.LoggedInUserView
import com.okada.rider.android.ui.login.LoginResult
import kotlin.Result

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class RegisterRepository(val accountService: AccountService, val dataService: DataService) {

    // in-memory cache of the loggedInUser object
    private var loggedInUser: LoggedInUser? = null

    val isLoggedIn: Boolean
        get() = loggedInUser != null

    fun fetchEmailAddress(completion: (Result<LoggedInUser>) -> Unit) {
        accountService.getLoggedInUser {result ->
            result.onSuccess {user->
                loggedInUser = LoggedInUser(user.userId,
                    user.email)
                completion(Result.success(loggedInUser!!))
            }
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