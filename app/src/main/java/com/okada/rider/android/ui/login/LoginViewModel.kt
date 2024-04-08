package com.okada.rider.android.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.okada.rider.android.Common
import com.okada.rider.android.data.AccountUsecase

import com.okada.rider.android.R
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.ui.splash.SplashResult

class LoginViewModel(private val accountUsecase: AccountUsecase, private val profileUsecase: ProfileUsecase) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        accountUsecase.login(username, password) { result ->
            result.fold(onSuccess = {user->
                //check if the logged in user has a profile
                profileUsecase.checkProfileExists(user) {result ->
                    result.fold(onSuccess = { profile ->
                        //check if the logged in user has a profile
                        Log.i("okada Log","LoginViewModel profile rxed ! ${profile!=null}")
                        profile?.also {user->
                            //-> Goto home screen
                            Log.i("okada Log","LoginViewModel Goto home screen!")
                            Common.currentUser = user
                            _loginResult.value =
                                LoginResult(navigateToHome = true)
                        } ?: run {
                            // No-> goto register screen
                            Log.i("okada Log","LoginViewModel Goto register screen!")
                            _loginResult.value =
                                LoginResult(navigateToRegister = true)
                        }
                    }, onFailure = {
                        // Error occurred
                        _loginResult.value = LoginResult(errorMsg = it.message)
                    })
                }
            },onFailure = {
                _loginResult.value = LoginResult(errorMsg = it.message)
            })
        }
    }

    fun loginDataChanged(username: String, password: String) {
        Log.i("okadaapp LoginViewModel:", "username: $username, password: $password")
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}