package com.okada.rider.android.data.model

import com.okada.rider.android.data.model.enums.JobStatus


data class JobInfoModel(
    var driverUid: String? = null,
    var clientUid: String? = null,
    var status: JobStatus? = null,
    var jobDetails: JobDetails? = null
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}

data class JobDetails(
    var type: String? = null,
    var info: String? = null,
    var distance: Int? = null,
    var distanceText: String? = null,
    var time: Int? = null,
    var timeText: String? = null,
    var price: Double? = null,
    var priceText: String? = null,
    var pickupLocation: AppLocation? = null,
    var deliveryLocation: AppLocation? = null,
    var driverLocation: AppLocation? = null,
    var pickupAddress: String? = null,
    var deliverAddress: String? = null
)


data class AppLocation (
    var latitude: Double? = null,
    var longitude: Double? = null
)


