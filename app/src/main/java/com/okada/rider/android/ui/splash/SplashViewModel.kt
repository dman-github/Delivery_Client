package com.okada.rider.android.ui.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.data.SignupUsecase

class SplashViewModel(
    private val accUsecase: AccountUsecase,
    private val profileUsecase: ProfileUsecase
) : ViewModel() {

    private val _splashResult = MutableLiveData<SplashResult>()
    private val splashResult: LiveData<SplashResult> = _splashResult

    private val _animationDone = MutableLiveData<Boolean>()
    private val animationDone: LiveData<Boolean> = _animationDone


    val liveDataMerger = MediatorLiveData<SplashResult>()

    fun removeLiveDataSources() {
        liveDataMerger.removeSource(splashResult)
        liveDataMerger.removeSource(animationDone)
        _animationDone.value = false

    }
    fun startSplashTimer() {
        Log.i("okada Log","startSplashTimer")
        Handler(Looper.getMainLooper()).postDelayed({
            _animationDone.value = true
            Log.i("okada Log","Time 3s done!")
        }, 3000)
        checkUserStatus()
        liveDataMerger.addSource(splashResult) { _ ->
            liveDataMerger.value = combineLatestData(splashResult, animationDone)
        }
        liveDataMerger.addSource(_animationDone) { _ ->
            liveDataMerger.value = combineLatestData(splashResult, animationDone)
        }
    }

    private fun combineLatestData(
        splashResult: LiveData<SplashResult>,
        animationDone: LiveData<Boolean>
    ): SplashResult {
        animationDone.value?.let { done ->
            splashResult.value?.let { result ->
                if (done) {
                    Log.i("okada Log","combineLatestData DONE!")
                    return result
                }
            }
        }
        return SplashResult() // all values are false or null
    }

    private fun checkUserStatus() {
        //check if onboarding has been done before
        //No -> Goto onboarding screen
        //check if there is a logged in user
        accUsecase.getLoggedInUser { result ->
            result.fold(onSuccess = { user ->
                //check if the logged in user has a profile
                profileUsecase.checkProfileExists(user) {result ->
                    result.fold(onSuccess = { exists ->
                        //check if the logged in user has a profile
                        if (exists) {
                            //-> Goto home screen
                            Log.i("okada Log","Goto home screen!")
                            _splashResult.value = SplashResult(navigateToHome = true)
                        } else {
                            // No-> goto register screen
                            Log.i("okada Log","Goto register screen!")
                            _splashResult.value = SplashResult(navigateToRegister = true)
                        }
                    }, onFailure = {
                        // Error occurred
                        _splashResult.value = SplashResult(errorMsg = it.message)
                    })
                }
            }, onFailure = {
                // No -> goto login screen
                Log.i("okada Log","Goto Login screen!")
                _splashResult.value = SplashResult(navigateToLogin = true)
            })
        }
    }


}
