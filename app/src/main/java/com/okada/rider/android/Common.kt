package com.okada.rider.android


import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.okada.rider.android.data.model.UserInfo
import kotlin.math.abs
import kotlin.math.atan

object Common {
    var currentUser: UserInfo? = null
    val NOTI_BODY: String = "body"
    val NOTI_TITLE: String = "title"

    fun buildFullname(): String {
        return StringBuilder(currentUser!!.firstname)
            .append(" ")
            .append(currentUser!!.lastname)
            .toString()
    }

    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        body: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val NOTIFICATION_CHANNEL_ID = "okada_client"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "okada_client",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "okada_client"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.baseline_drive_eta_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.baseline_drive_eta_24
                )
            )
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        notificationManager.notify(id, notification)

    }

    //DECODE POLY
    // A polynomial curve comprising of multiple lat,lon coordinates is encoded in to a base64 string
    // This is the input to this function and the outputs is the list of lat,long coordinates that
    // the string comprises of.
    fun decodePoly(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].digitToInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].digitToInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) -(result shr 1) else (result shr 1)
            lng += dlng

            val p = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(p)
        }
        return poly
    }

    //GET BEARING
    // This is the angle in degrees you begin with when travelling from Point A to Point B
    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = abs(begin.latitude - end.latitude)
        val lng = abs(begin.longitude - end.longitude)

        return when {
            begin.latitude < end.latitude && begin.longitude < end.longitude -> {
                (Math.toDegrees(atan(lng / lat))).toFloat()
            }
            begin.latitude >= end.latitude && begin.longitude < end.longitude -> {
                ((90 - Math.toDegrees(atan(lng / lat))) + 90).toFloat()
            }
            begin.latitude >= end.latitude && begin.longitude >= end.longitude -> {
                (Math.toDegrees(atan(lng / lat)) + 180).toFloat()
            }
            begin.latitude < end.latitude && begin.longitude >= end.longitude -> {
                ((90 - Math.toDegrees(atan(lng / lat))) + 270).toFloat()
            }
            else -> -1f
        }
    }

    fun computeRotationNew(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360
        val direction =
            (if (normalizedEndAbs > 180) -1 else 1).toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        rotation = if (direction > 0) {
            normalizedEndAbs
        } else {
            normalizedEndAbs - 360
        }
        val result = fraction * rotation + start
        return (result + 360) % 360
    }

    fun formatDuration(duration: String): CharSequence? {
        if (duration.contains("mins")) {
            val firstIndexMins = duration.indexOf("mins")
            // Remove the space+mins from the duration and add a carriage return
            return "${duration.substring(0, firstIndexMins - 1)}\nmins"
        }
        else
            return duration
    }

    fun formatAddress(startAddress: String): CharSequence? {
        val firstIndexComma = startAddress.indexOf(",")
        return startAddress.substring(0,firstIndexComma)
    }

    fun valueAnimate(duration: Int, listener: AnimatorUpdateListener): ValueAnimator {
        val va = ValueAnimator.ofFloat(0f,100f)
        va.duration = duration.toLong()
        va.addUpdateListener(listener)
        va.repeatCount = ValueAnimator.INFINITE
        va.repeatMode = ValueAnimator.RESTART
        va.start()
        return va
    }

    fun isDarkMode(context: Context): Boolean {
        val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
    }


}
