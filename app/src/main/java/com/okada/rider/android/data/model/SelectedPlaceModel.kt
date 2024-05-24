package com.okada.rider.android.data.model

import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng

class SelectedPlaceModel(
    var polylineList: List<LatLng>? = null,
    var boundedTime: String? = null,
    var startAddress: String? = null,
    var endAddress: String? = null,
    var eventOrigin: LatLng? = null,
    var eventDest: LatLng? = null
) {

}
