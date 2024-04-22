package com.okada.rider.android.services

import android.content.Context
import android.location.Location
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.ValueEventListener

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
        completion: (Result<String>) -> Unit,
        geoQueryEventListener: GeoQueryEventListener,
        childEventListener: ChildEventListener
    )

    fun addDriverListener(uid: String, subdomain: String, listener: ValueEventListener)

    fun removeAllListeners(subdomain: String)
}