package com.okada.rider.android.data.model

import com.firebase.geofire.GeoLocation

class DriverGeoModel(
    var key: String? = null,
    var geoLocation: GeoLocation? = null,
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


}
