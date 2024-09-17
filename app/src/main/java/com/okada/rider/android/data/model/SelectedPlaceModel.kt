package com.okada.rider.android.data.model

import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng

class SelectedPlaceModel(
    var polylineList: List<LatLng>? = null,
    var duration: Int? = null,
    var durationText: String? = null,
    var distance: Int? = null,
    var distanceText: String? = null,
    var startAddress: String? = null,
    var endAddress: String? = null,
    var eventOrigin: LatLng? = null,
    var eventDest: LatLng? = null,
    var price: Double? = null
) {

}
