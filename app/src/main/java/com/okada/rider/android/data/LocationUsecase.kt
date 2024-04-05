package com.okada.rider.android.data

import android.location.Location
import com.okada.rider.android.services.LocationService

class LocationUsecase(val locationService: LocationService) {
    fun setupDatabase(uid: String) {
        locationService.setupDatabase(uid)
    }

    fun updateLocation(
        uid:String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit
    ) {
        locationService.updateLocation(uid,newLocation,completion)
    }

    fun removeLocationFor(uid: String) {
        locationService.removeLocationfor(uid)
    }
}