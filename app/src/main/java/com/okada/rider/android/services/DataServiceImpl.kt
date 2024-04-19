package com.okada.rider.android.services

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.TokenModel
import com.okada.rider.android.data.model.UserInfo

class DataServiceImpl: DataService {
    private val databaseRefUser = FirebaseDatabase.getInstance().getReference("UserInfo")
    private val databaseRefDriverInfo = FirebaseDatabase.getInstance().getReference("DriverInfo")
    private val pushTokenRef = FirebaseDatabase.getInstance().getReference("PushTokens")
    override fun checkIfUserInfoExists(uid: String, listener: ValueEventListener) {
        // Set up Firebase listener
        databaseRefUser.child(uid).addListenerForSingleValueEvent(listener)
    }

    override fun createUserInfo(uid: String, userInfo: UserInfo,
                                failureListener: OnFailureListener,
                                successListener: OnSuccessListener<Void>) {
        databaseRefUser.child(uid)
            .setValue(userInfo)
            .addOnFailureListener(failureListener)
            .addOnSuccessListener(successListener)
    }

    override fun updatePushMessagingToken(uid: String, tokenModel: TokenModel, completion: (Result<Unit>) -> Unit) {
        pushTokenRef.child(uid)
            .setValue(tokenModel)
            .addOnFailureListener {e ->
                completion(Result.failure(e))
            }.addOnSuccessListener {
                completion(Result.success(Unit))
            }
    }

    override fun fetchDriverInfo(uid: String, listener: ValueEventListener) {
        // Set up Firebase listener
        databaseRefDriverInfo.child(uid).addListenerForSingleValueEvent(listener)
    }


}