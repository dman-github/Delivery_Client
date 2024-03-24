package com.okada.rider.android.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.okada.rider.android.data.AccountUsecase

import com.okada.rider.android.R

class LoginViewModel(private val loginRepository: AccountUsecase) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        loginRepository.login(username, password) {result ->
            result.fold(onSuccess = {
                if (loginRepository.profileExists) {
                    _loginResult.value =
                        LoginResult(navigateToHome = true)
                } else {
                    _loginResult.value =
                        LoginResult(navigateToRegister = true)
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