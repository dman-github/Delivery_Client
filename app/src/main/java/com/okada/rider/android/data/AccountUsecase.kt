package com.okada.rider.android.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.data.model.UserInfo
import com.okada.rider.android.services.AccountService
import com.okada.rider.android.services.DataService

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class AccountUsecase (val accountService: AccountService) {

    // in-memory cache of the loggedInUser object
    var loggedInUser: LoggedInUser?

    val isLoggedIn: Boolean
        get() = loggedInUser != null

    var profileExists: Boolean = false

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        loggedInUser = null
    }

    fun logout(completion: (Result<Unit>) -> Unit) {
        accountService.logout(completion)
    }

    fun getLoggedInUser(completion: (Result<LoggedInUser>) -> Unit) {
        accountService.getLoggedInUser {result ->
            result.onSuccess {user->
                loggedInUser = LoggedInUser(user.userId,
                    user.email)
                completion(Result.success(loggedInUser!!))
            }
            result.onFailure {
                completion(Result.failure(it))
            }
        }
    }

    fun login(username: String, password: String, completion: (Result<LoggedInUser>) -> Unit) {
        // handle login
        accountService.authenticate(username, password) {result->
            result.fold(onSuccess = {user->
                loggedInUser = LoggedInUser(user.userId,user.email)
                completion(Result.success(loggedInUser!!))
            },onFailure = {
                completion(Result.failure(it))
            })
        }
    }

    fun fetchPushNotificationToken(completion: (Result<String>) -> Unit) {
        accountService.getPushNotificationToken(completion)
    }
}