package com.okada.rider.android.services

import android.location.Location

interface LocationService {

    fun setupDatabase(uid: String)

    fun updateLocation(
        uid:String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit)

    fun removeLocationfor(uid: String)
}