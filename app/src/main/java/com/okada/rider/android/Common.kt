package com.okada.rider.android


import com.okada.rider.android.data.model.UserInfo

object Common {

    fun buildFullname(): String {
        return StringBuilder(currentUser!!.firstname)
            .append(" ")
            .append(currentUser!!.lastname)
            .toString()
    }
    var currentUser: UserInfo? = null
}
