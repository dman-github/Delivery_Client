package com.okada.rider.android.data.model

import com.firebase.geofire.GeoLocation

class DriverGeoModel(
    // This is the key to identify the driver in the DB
    var key: String? = null,
    // Current Lat, Lan coordinates
    var geoLocation: GeoLocation? = null,
    // Driver profile information
    var driverInfoModel: DriverInfo? = null
) {
    constructor(key: String?, geoLocation: GeoLocation?) : this() {
        this.key = key
        this.geoLocation = geoLocation
    }

    override fun hashCode(): Int {
        return key?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DriverGeoModel

        return key == other.key
    }

    fun getLocationStr(): String {
        var result = ""
        geoLocation?.let { loc ->
            result =  StringBuilder().append(loc.latitude).append(",").append(loc.longitude).toString()
        }
        return result
    }


}
