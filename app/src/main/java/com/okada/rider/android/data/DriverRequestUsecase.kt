package com.okada.rider.android.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.model.GeoQueryModel
import com.okada.rider.android.data.model.TokenModel
import com.okada.rider.android.services.DataService
import com.okada.rider.android.services.DriverRequestService

class DriverRequestUsecase (
    val driverRequestService: DriverRequestService,
    val dataService: DataService
) {
    fun sendDriverRouteRequest(
        driverUid: String,
        userUid: String,
        pickupLocation: LatLng,
        completion: (Result<Unit>) -> Unit
    ) {
        dataService.retrievePushMessagingToken(driverUid, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChildren()) {

                } else {
                    snapshot.getValue(TokenModel::class.java)?.let { model ->
                        driverRequestService.sendDriverRouteRequest(
                            pickupLocation,
                            model.token,
                            userUid,
                        ) { result ->
                            result.fold(onSuccess = {
                                // Notification sent
                                completion(Result.success(Unit))
                            }, onFailure = {
                                // Error occurred
                                completion(Result.failure(it))
                            })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                completion(Result.failure(error.toException()))
            }
        })
    }
}