package com.okada.rider.android.data.model

import android.location.Location
import com.firebase.geofire.GeoLocation
import com.okada.rider.android.Common

class MarkerModel (
    var firstName: String,
    var surname: String,
    var driverLat: Double,
    var driverLong: Double,
    var rating: Double,
    var uid: String
) {

    fun getMarkerTitle(): String {
        return StringBuilder(firstName)
            .append(" ")
            .append(surname)
            .toString()
    }

    fun getRating(): String {
        return "$rating"
    }
}