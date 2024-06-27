package com.okada.rider.android.data.model

import com.okada.rider.android.data.model.enums.JobStatus


data class JobInfoModel (var jobId: String? = null,
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
    var pickupLocation: Location? = null,
    var deliveryLocation: Location? = null
)


data class Location(
    var latitude: Double? = null,
    var longitude: Double? = null
)


