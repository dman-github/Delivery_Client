package com.okada.rider.android.services

import android.location.Location
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.UserInfo

class LocationServiceImpl : LocationService {
    // Online database
    private lateinit var databaseRefUserLocations: DatabaseReference
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserLocationRef: DatabaseReference
    private lateinit var geoFireRef: GeoFire

    override fun setupDatabase(uid: String) {
        // Setting up Driver location DB
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        databaseRefUserLocations = FirebaseDatabase.getInstance().getReference("ClientLocations")
        currentUserLocationRef = databaseRefUserLocations.child(uid)
        geoFireRef = GeoFire(databaseRefUserLocations)
    }

    override fun updateLocation(
        uid:String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit
    ) {
        geoFireRef.setLocation(uid, GeoLocation(newLocation.latitude, newLocation.longitude)
        ) { _: String?, error: DatabaseError? ->
            if (error != null) {
                completion(Result.failure(Exception(error.message)))
            } else {
                completion(Result.success(Unit))
            }
        }
    }
    override fun removeLocationfor(uid: String) {
        geoFireRef.removeLocation(uid)
    }

/*
    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    private val onlineValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                currentUserRef.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _showSnackbarMessage.value = error.message
        }
    }
    */
}