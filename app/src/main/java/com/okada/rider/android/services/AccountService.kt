package com.okada.rider.android.services

import com.okada.rider.android.data.model.LoggedInUser

interface AccountService {
    fun authenticate(email: String, password: String, completion: (Result<LoggedInUser>) -> Unit)
    fun isUserLoggedIn(completion: (Result<Boolean>) -> Unit)
    fun logout(completion: (Result<Unit>) -> Unit )

}