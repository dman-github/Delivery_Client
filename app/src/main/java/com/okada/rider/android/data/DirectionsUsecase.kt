package com.okada.rider.android.data

import com.google.maps.android.PolyUtil
import com.okada.rider.android.data.model.SelectedPlaceModel
import com.okada.rider.android.services.DirectionsService
import com.okada.rider.android.services.remote.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
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
        completion: (Result<SelectedPlaceModel>) -> Unit
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
                            var placeModel = SelectedPlaceModel()
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
                                placeModel.polylineList = PolyUtil.decode(polyline)
                            }
                            val objects = jsonArray.getJSONObject(0)
                            val legs = objects.getJSONArray("legs")
                            val legsObject = legs.getJSONObject(0)

                            val time = legsObject.getJSONObject(("duration"))
                            val duration = time.getString("text")
                            placeModel.boundedTime = duration
                            placeModel.startAddress = legsObject.getString("start_address")
                            placeModel.endAddress = legsObject.getString("end_address")
                            completion(Result.success(placeModel))
                        } catch (e: Exception) {
                            completion(Result.failure(e))
                        }
                    } ?: run {
                        completion(Result.failure(Exception("Did not get any direction information")))
                    }
                }
        )
    }

    fun getAddressForLocation(
        at: String,
        apiKey: String,
        completion: (Result<Pair<String, String>>) -> Unit
    ) {
        //fetch address information at a point
        compositeDisposable.add(
            directionsService.getAddress(
                at, apiKey
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { returnResult ->
                    returnResult?.let { result ->
                        try {
                            val jsonObject = JSONObject(result)
                            if (jsonObject.getString("status").lowercase() == "ok") {
                                val results = jsonObject.getJSONArray("results")
                                if (results.length() > 0) {
                                    val addressComponents =
                                        results.getJSONObject(0).getJSONArray("address_components")
                                    val components = parseAddressComponents(addressComponents)
                                    completion(Result.success(components))
                                } else {
                                    completion(Result.failure(Exception("Did not get any address information")))
                                }
                            }
                        } catch (e: Exception) {
                            completion(Result.failure(e))
                        }
                    } ?: run {
                        completion(Result.failure(Exception("Did not get any address information")))
                    }
                }
        )
    }

    private fun parseAddressComponents(addressComponents: JSONArray): Pair<String, String> {
        var streetNumber = ""
        var route = ""
        var locality = ""
        var sublocality = ""

        for (i in 0 until addressComponents.length()) {
            val component = addressComponents.getJSONObject(i)
            val types = component.getJSONArray("types")
            val longName = component.getString("long_name")

            when {
                types.toString().contains("street_number") -> streetNumber = longName
                types.toString().contains("route") -> route = longName
                types.toString().contains("locality") -> locality = longName
                types.toString().contains("sublocality") -> sublocality = longName
            }
        }

        val firstLine = "$streetNumber $route"
        val secondLine = sublocality.ifEmpty { locality }
        return Pair(firstLine, secondLine)
    }
}