package com.okada.rider.android.services

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.okada.rider.android.Common
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.TokenModel
import java.util.Random

class FirebaseMessagingIdService: FirebaseMessagingService() {


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        data.let {
            Common.showNotification(this, Random().nextInt(), data[Common.NOTI_TITLE], data[Common.NOTI_BODY],null)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val model = TokenModel()
        model.token = token
        FirebaseAuth.getInstance().currentUser?.let {user->
            DataServiceImpl().updatePushMessagingToken(user.uid,model){result->
                result.fold(onSuccess = {
                    //check if the logged in user has a profile
                    Log.i("App_info","FirebaseMessagingIdService, Token sent: $token")
                }, onFailure = {
                })
            }
        }
    }
}