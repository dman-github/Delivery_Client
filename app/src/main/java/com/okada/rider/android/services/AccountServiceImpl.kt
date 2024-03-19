package com.okada.rider.android.services

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.LoggedInUser

class AccountServiceImpl: AccountService {
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

    override fun logout(completion: (Result<Unit>) -> Unit) {
        FirebaseAuth.getInstance().signOut()
        completion(Result.success(Unit))
    }





}