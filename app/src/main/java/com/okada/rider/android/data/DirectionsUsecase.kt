package com.okada.rider.android.data

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.services.DirectionsService
import com.okada.rider.android.services.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class DirectionsUsecase() {
    private var directionsService: DirectionsService =
        RetrofitClient.instance!!.create(DirectionsService::class.java)
    val compositeDisposable: CompositeDisposable = CompositeDisposable()


    fun closeConnection() {
        compositeDisposable.clear()
    }

    fun getDirections(
        from: String,
        to: String,
        apiKey: String,
        completion: (Result<List<LatLng>>) -> Unit
    ) {
        //fetch directions between the 2 points
        compositeDisposable.add(
            directionsService.getDirections(
                "driving",
                "less_driving", from, to, apiKey
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { returnResult ->
                    returnResult?.let { result ->
                        try {
                            val jsonObject = JSONObject(result)
                            val errorString = jsonObject.getString("status")
                            if (errorString.isNotEmpty() && errorString.lowercase() != "ok") {
                                completion(Result.failure(Exception("Directions api: $errorString")))
                            }
                            val jsonArray = jsonObject.getJSONArray("routes")
                            for (i in 0 until jsonArray.length()) {
                                val route = jsonArray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                completion(Result.success(PolyUtil.decode(polyline)))
                            }
                        } catch (e: Exception) {
                            completion(Result.failure(e))
                        }
                    } ?: run {
                        completion(Result.failure(Exception("Did not get any direction information")))
                    }
                }
        )
    }
}