package com.okada.rider.android.services

import com.google.firebase.auth.FirebaseAuth
import com.okada.rider.android.login.data.model.LoggedInUser

class AccountServiceImpl: AccountService {

    override fun authenticate(email: String, password: String, completion: (Result<LoggedInUser>) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val uid = it.result.user!!.uid
                completion(Result.success(LoggedInUser(uid)))
            } else {
                it.exception?.also {exception->
                    completion(Result.failure(exception))
                }?:run {
                    completion(Result.failure(Exception("Network Error")))
                }
            }
        }
    }

    override fun logout(completion: (Result<Unit>) -> Unit) {
        completion(Result.success(Unit))
    }
}