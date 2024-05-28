package com.okada.rider.android.ui.home

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.okada.rider.android.data.model.AnimationModel
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.MarkerModel

class HomeModel {
    var apiKey: String = ""
    var distance = 20.0
    var range_limit = 20.0
    var uid: String? = null
    var currentLocation: Location? = null
    var previousLocation: Location? = null
    var currentAddress: String = ""
    var firstTime: Boolean = true
    var domain: String? = null
    var dropAddress: LatLng? = null
    var pickupAddress: LatLng? = null
    var nearestDrivers: MutableSet<DriverGeoModel>  =  HashSet()
    var mapMarkers: MutableMap<String, Marker> = HashMap()
    var driversSubscribed: MutableMap<String, AnimationModel> = HashMap()
}