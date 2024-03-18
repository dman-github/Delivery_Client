package com.okada.rider.android.services

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.UserInfo

interface DataService {
    fun checkIfUserInfoExists(uid: String, listener: ValueEventListener)
    fun createUserInfo(uid: String, userInfo: UserInfo,
                                failureListener: OnFailureListener,
                                successListener: OnSuccessListener<Void>)
}