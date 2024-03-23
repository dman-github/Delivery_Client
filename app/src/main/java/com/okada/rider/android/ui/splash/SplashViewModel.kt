package com.okada.rider.android.ui.splash

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.okada.rider.android.R
import com.okada.rider.android.data.SignupRepository
import kotlinx.coroutines.flow.combine

class SplashViewModel(private val signupRepository: SignupRepository) : ViewModel() {

    private val _splashResult = MutableLiveData<SplashResult>()
    private val splashResult: LiveData<SplashResult> = _splashResult

    private val _animationDone = MutableLiveData<Boolean>()
    private val animationDone: LiveData<Boolean> = _animationDone


   val liveDataMerger = MediatorLiveData<SplashResult>()
    fun startSplashTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            _animationDone.value = true
        }, 3000)
        checkUserStatus()
        liveDataMerger.addSource(splashResult) { value ->
            liveDataMerger.value = combineLatestData(splashResult, animationDone)
        }
        liveDataMerger.addSource(_animationDone) { value ->
            liveDataMerger.value = combineLatestData(splashResult, animationDone)
        }
    }

    private fun combineLatestData(
        splashResult: LiveData<SplashResult>,
        animationDone: LiveData<Boolean>
    ): SplashResult {
        animationDone.value?.let {done->
            splashResult.value?.let{result->
                if (done) {
                    return result
                }
            }
        }
        return SplashResult() // all values are false or null
    }

    fun  checkUserStatus() {
        //check if onboarding has been done before
            //No -> Goto onboarding screen
        //check if there is a logged in user
            // No -> goto login screen
        //check if the logged in user has a profile
            // No-> goto register screen
        //-> Goto home screen
    }


}
