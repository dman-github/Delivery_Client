package com.okada.rider.android.login.data

import com.okada.rider.android.login.data.model.LoggedInUser
import com.okada.rider.android.services.AccountService

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val accountService: AccountService) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout(completion: (kotlin.Result<Unit>) -> Unit) {
        accountService.logout(completion)
    }

    fun login(username: String, password: String, completion: (kotlin.Result<LoggedInUser>) -> Unit) {
        // handle login
        accountService.authenticate(username, password, completion)
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}