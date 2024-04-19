package com.okada.rider.android.data

import android.content.Context
import android.location.Location
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.ChildEventListener
import com.okada.rider.android.services.LocationService

class LocationUsecase(val locationService: LocationService) {
    fun setupDatabase(uid: String) {
        locationService.setupDatabase(uid)
    }

    fun updateLocation(
        uid: String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit
    ) {
        locationService.updateLocation(uid, newLocation, completion)
    }

    fun removeLocationFor(uid: String) {
        locationService.removeLocationfor(uid)
    }

    fun fetchNearestDrivers(
        location: Location,
        distance: Double,
        context: Context,
        completion: (Result<Unit>) -> Unit,
        geoQueryEventListener: GeoQueryEventListener,
        childEventListener: ChildEventListener
    ) {
        locationService.fetchNearestDrivers(
            location,
            distance,
            context,
            completion,
            geoQueryEventListener,
            childEventListener
        )
    }
}