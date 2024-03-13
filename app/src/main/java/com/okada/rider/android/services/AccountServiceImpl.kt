package com.okada.rider.android.services

import com.okada.rider.android.login.data.model.LoggedInUser

class AccountServiceImpl: AccountService {

    override fun authenticate(email: String, password: String, completion: (Result<LoggedInUser>) -> Unit) {
        completion(Result.success(LoggedInUser("test")))
    }

    override fun logout(completion: (Result<Unit>) -> Unit) {
        completion(Result.success(Unit))
    }
}