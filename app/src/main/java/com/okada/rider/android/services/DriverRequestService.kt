package com.okada.rider.android.services

import com.okada.rider.android.data.model.LoggedInUser
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface DriverRequestService {

    fun sendDriverRouteRequest(completion: (Result<Unit>) -> Unit)

}