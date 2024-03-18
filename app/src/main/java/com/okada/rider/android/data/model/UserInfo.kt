package com.okada.rider.android.data.model

data class UserInfo (var firstname: String? = null,
                     var lastname: String? = null,
                     var email: String? = null,
                     var biometricId: String? = null,
                     var rating: Double = 0.0,
                     var avatar: String? = null
              ) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

}