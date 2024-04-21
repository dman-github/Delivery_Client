package com.okada.rider.android.services

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.UserInfo
import java.io.IOException
import java.util.Locale

class LocationServiceImpl : LocationService {
    // Online database
    private lateinit var databaseRefUserLocations: DatabaseReference
    private lateinit var databaseRefDriverLocations: DatabaseReference
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserLocationRef: DatabaseReference
    private lateinit var geoFireUserRef: GeoFire
    private lateinit var geoFireDriverRef: GeoFire
    private var geoQuery: GeoQuery? = null
    override fun setupDatabase(uid: String) {
        // Setting up Driver location DB
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        databaseRefUserLocations = FirebaseDatabase.getInstance().getReference("ClientLocations")
        databaseRefDriverLocations = FirebaseDatabase.getInstance().getReference("DriverLocations")
        currentUserLocationRef = databaseRefUserLocations.child(uid)
        geoFireUserRef = GeoFire(databaseRefUserLocations)
        geoFireDriverRef = GeoFire(databaseRefDriverLocations)
    }

    override fun updateLocation(
        uid: String,
        newLocation: Location,
        completion: (Result<Unit>) -> Unit
    ) {
        geoFireUserRef.setLocation(
            uid, GeoLocation(newLocation.latitude, newLocation.longitude)
        ) { _: String?, error: DatabaseError? ->
            if (error != null) {
                completion(Result.failure(Exception(error.message)))
            } else {
                completion(Result.success(Unit))
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun fetchNearestDrivers(
        location: Location,
        distance: Double,
        context: Context,
        completion: (Result<Unit>) -> Unit,
        geoQueryEventListener: GeoQueryEventListener,
        childEventListener: ChildEventListener
    ) {
        // In Android Tiramisu onwards the getFromLocation has been deprecated in favour of an async trigger function
        // instead of the in-line function
        val geoCoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            try {
                val addressList =
                    geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                queryDrivers(
                    location,
                    distance,
                    addressList?.firstOrNull(),
                    completion,
                    geoQueryEventListener,
                    childEventListener
                )
            } catch (e: IOException) {
                completion(Result.failure(e))
            }
        } else {
            geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            ) { addresses ->
                queryDrivers(
                    location,
                    distance,
                    addresses.firstOrNull(),
                    completion,
                    geoQueryEventListener,
                    childEventListener
                )
            }
        }

    }

    private fun queryDrivers(
        location: Location,
        distance: Double,
        address: Address?,
        completion: (Result<Unit>) -> Unit,
        geoQueryEventListener: GeoQueryEventListener,
        childEventListener: ChildEventListener
    ) {
        address?.let {
            val geocodeLocation =
                getCountryCodeComponent(address) + "_" + getGeocodeComponent(address)
            geoFireDriverRef = GeoFire(databaseRefDriverLocations.child(geocodeLocation))
            // There should ony be one query instance and remove all listeners before adding new ones
            geoQuery?.removeAllListeners()
            geoQuery = geoFireDriverRef.queryAtLocation(
                GeoLocation(location.latitude, location.longitude),
                distance
            )
            geoQuery?.addGeoQueryEventListener(geoQueryEventListener)
            //databaseRefDriverLocations.addChildEventListener(childEventListener)
        } ?: run {
            completion(Result.failure(Exception("Cannot find address from Geocode")))
        }
    }

    private fun getGeocodeComponent(a: Address): String {
        //locality -> adminArea is going from narrow window to a larger address window
        // if the locality is null then select sublocality e.t.`false`
        // Normally locality is the city
        return a.locality ?: a.subLocality ?: a.subAdminArea ?: a.adminArea
    }

    private fun getCountryCodeComponent(a: Address): String {
        //locality -> adminArea is going from narrow window to a larger address window
        // if the locality is null then select sublocality e.t.`false`
        // Normally locality is the city
        return a.countryCode ?: ""
    }

    override fun removeLocationfor(uid: String) {
        geoFireUserRef.removeLocation(uid)
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