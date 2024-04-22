package com.okada.rider.android.data

import android.content.Context
import android.location.Location
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener
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
        completion: (Result<String>) -> Unit,
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

    fun addDriverListener(uid: String, subdomain: String, listener: ValueEventListener) {
        locationService.addDriverListener(uid,subdomain,listener)
    }

    fun removeAllListeners(subdomain: String) {
        locationService.removeAllListeners(subdomain)
    }
}