package com.okada.rider.android.services

import android.content.Context
import android.location.Location
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.ChildEventListener

interface LocationService {

    fun setupDatabase(uid: String)

    fun updateLocation(
        uid:String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit)

    fun removeLocationfor(uid: String)

    fun fetchNearestDrivers(
        location: Location,
        distance: Double,
        context: Context,
        completion: (Result<Unit>) -> Unit,
        geoQueryEventListener: GeoQueryEventListener,
        childEventListener: ChildEventListener
    )
}