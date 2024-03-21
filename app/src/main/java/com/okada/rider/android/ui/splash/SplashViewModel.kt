package com.okada.rider.android.ui.splash

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.R
import com.okada.rider.android.data.SignupRepository

class SplashViewModel(private val signupRepository: SignupRepository) : ViewModel() {

    private val _signupResult = MutableLiveData<SignupResult>()
    val signupResult: LiveData<SignupResult> = _signupResult

    fun signup(username: String, password: String) {
        signupRepository.createUser(username, password) {result ->
            result.fold(onSuccess = {
                _signupResult.value =
                    SignupResult(navigateToRegister = true)

            },onFailure = {
                _signupResult.value = SignupResult(errorMsg = it.message)
            })
        }
    }


}
