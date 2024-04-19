package com.okada.rider.android.ui.home

import android.location.Location
import com.okada.rider.android.data.model.DriverGeoModel
import com.okada.rider.android.data.model.MarkerModel

class HomeModel {
    var distance = 1.0
    var range_limit = 10
    var uid: String? = null
    var currentLocation: Location? = null
    var previousLocation: Location? = null
    var firstTime: Boolean = true
    var nearestDrivers: MutableSet<DriverGeoModel>  =  HashSet()
    var mapMarkers: MutableMap<String,MarkerModel> = HashMap()
}