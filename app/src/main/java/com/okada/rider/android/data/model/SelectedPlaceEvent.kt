package com.okada.rider.android.data.model

import com.google.android.gms.maps.model.LatLng

class SelectedPlaceEvent(
    var origin: LatLng, var destination: LatLng,
    var originAddress: String, var destAddress: String,
    var duration: Int = -1, var distance: Int = -1,
    var durationText: String = "", var distanceText: String = "",
    var price: Double = -1.0,  var priceText: String = ""
) {
    val originString: String
        get() = StringBuilder().append(origin.latitude).append(",").append(origin.longitude)
            .toString()
    val destString: String
        get() = StringBuilder().append(destination.latitude).append(",")
            .append(destination.longitude).toString()

}