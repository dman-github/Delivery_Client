package com.okada.rider.android.ui.requestDriver

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.okada.rider.android.data.model.AppLocation

class RequestDriverModel {
    var apiKey: String = ""
    var declinedDrivers =  mutableListOf<String>()
    var jobDriverCurrentLocation: AppLocation = AppLocation(0.0, 0.0)
    var plotDriverToPickup = false
    var plotDriverToDest = false
}