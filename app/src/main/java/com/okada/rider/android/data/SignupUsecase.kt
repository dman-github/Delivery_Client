package com.okada.rider.android.data

import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.services.AccountService
import kotlin.Result

class SignupUsecase(val accountService: AccountService) {

    // in-memory cache of the loggedInUser object
    private var loggedInUser: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = loggedInUser != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        loggedInUser = null
    }
    fun createUser(email: String,
                   password: String,
                   completion: (Result<LoggedInUser>) -> Unit) {
        accountService.createUser(email, password) {result ->
            result.fold(onSuccess = {user->
                loggedInUser = LoggedInUser(user.userId,
                    user.email)
                completion(Result.success(loggedInUser!!))
            },onFailure = {
                completion(Result.failure(it))
            })
        }

    }
}