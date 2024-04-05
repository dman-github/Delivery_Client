package com.okada.rider.android.ui.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.LocationUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.model.LoggedInUser
import com.okada.rider.android.ui.login.LoginFormState
import com.okada.rider.android.ui.login.LoginResult
import com.okada.rider.android.ui.register.RegisterFormState
import com.okada.rider.android.ui.register.RegisterResult

class HomeViewModel(private val accountUsecase: AccountUsecase,
                        private val locationUsecase: LocationUsecase
) : ViewModel() {


    private val _showSnackbarMessage = MutableLiveData<String?>()
    val showSnackbarMessage: LiveData<String?> = _showSnackbarMessage

    private val _updateMap = MutableLiveData<LatLng>()
    val updateMap: LiveData<LatLng> = _updateMap

    //Model
    private val _model = HomeModel()

    // Online database
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var driverLocationRef: DatabaseReference
    private lateinit var geoFire: GeoFire


    init {
        accountUsecase.getLoggedInUser { result ->
            result.fold(onSuccess = { user ->
                _model.uid = user.userId
                locationUsecase.setupDatabase(user.userId)
            }, onFailure = {
                _showSnackbarMessage.value = it.message
            })
        }
    }

    fun clearMessage() {
        _showSnackbarMessage.value = null
    }

    fun updateLocation(location: Location?) {
        location?.let {location ->
            val newPos = LatLng(location.latitude, location.longitude)
            _model.uid?.also {uid->
                locationUsecase.updateLocation(uid, location) {result->
                    result.onSuccess {
                        _model.lastLocation = newPos
                        _updateMap.value = newPos
                        _showSnackbarMessage.value = "Location updated\nLat: ${location.latitude}, Lon: ${location.longitude}}"
                    }
                    result.onFailure {
                        _showSnackbarMessage.value = it.message
                    }
                }
            }?:run {
                _showSnackbarMessage.value = "No logged in user"
            }
        }
    }

    fun removeUserLocation() {
        _model.uid?.also {uid->
            locationUsecase.removeLocationFor(uid)
        }?:run {
            _showSnackbarMessage.value = "No logged in user"
        }
    }



}