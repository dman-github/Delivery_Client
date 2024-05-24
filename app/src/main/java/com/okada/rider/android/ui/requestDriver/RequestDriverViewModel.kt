package com.okada.rider.android.ui.requestDriver

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.DirectionsUsecase
import com.okada.rider.android.data.LocationUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.MarkerModel
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.data.model.SelectedPlaceModel

class RequestDriverViewModel(
    private val accountUsecase: AccountUsecase,
    private val locationUsecase: LocationUsecase,
    private val profileUsecase: ProfileUsecase,
    private val directionsUsecase: DirectionsUsecase
) : ViewModel() {


    private val _showMessage = MutableLiveData<String?>()
    val showMessage: LiveData<String?> = _showMessage

    private val _updateMapPolyLines = MutableLiveData<PolylineOptions>()
    val updateMapPolyLines: LiveData<PolylineOptions> = _updateMapPolyLines

    private val _updateMap = MutableLiveData<SelectedPlaceModel>()
    val updateMap: LiveData<SelectedPlaceModel> = _updateMap

    //Model
    private val _model = RequestDriverModel()


    fun clearMessage() {
        _showMessage.value = null
    }

    fun setGoogleApiKey(key: String) {
        _model.apiKey = key
    }

    fun viewWillStop() {
        directionsUsecase.closeConnection()
    }



    fun calculatePath(event: SelectedPlaceEvent) {
            //fetch directions between the 2 points from the Google directions api
            directionsUsecase.getDirections(event.originString, event.destString, _model.apiKey) { result ->
                result.onSuccess { placeModel ->
                    try {
                        placeModel.eventOrigin = event.origin
                        placeModel.eventDest = event.destination
                        _updateMap.value = placeModel
                    } catch (e: Exception) {
                        _showMessage.value = e.message
                    }
                }
                result.onFailure {
                    _showMessage.value = it.message
                }
            }
    }

}