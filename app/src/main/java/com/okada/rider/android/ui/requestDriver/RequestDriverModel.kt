package com.okada.rider.android.ui.requestDriver

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class RequestDriverModel {
    var apiKey: String = ""
    var declinedDrivers =  mutableListOf<String>()
}