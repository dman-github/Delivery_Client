package com.okada.rider.android.services

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.okada.rider.android.Common
import com.okada.rider.android.Common.CLIENT_KEY
import com.okada.rider.android.Common.DECLINE_REQUEST_MSG_TITLE
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.DeclineRequestEvent
import com.okada.rider.android.data.model.TokenModel
import org.greenrobot.eventbus.EventBus
import java.util.Random

class FirebaseMessagingIdService : FirebaseMessagingService() {


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Firebase message has a Notification object and a Data object. The Data object is a dictionary
        val data = message.data
        message.notification?.let { noti ->
            if (noti.title.equals(DECLINE_REQUEST_MSG_TITLE)) {
                data[CLIENT_KEY]?.let { key ->
                    EventBus.getDefault().postSticky(DeclineRequestEvent(key))
                }
            } else {
                Common.showNotification(
                    this,
                    Random().nextInt(),
                    noti.title,
                    noti.body,
                    null
                )
            }

        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val model = TokenModel()
        model.token = token
        FirebaseAuth.getInstance().currentUser?.let { user ->
            DataServiceImpl().updatePushMessagingToken(user.uid, model) { result ->
                result.fold(onSuccess = {
                    //check if the logged in user has a profile
                    Log.i("App_info", "FirebaseMessagingIdService, Token sent: $token")
                }, onFailure = {
                })
            }
        }
    }


}