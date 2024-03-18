package com.okada.rider.android.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.data.RegisterRepository

import com.okada.rider.android.R
import com.okada.rider.android.ui.login.LoggedInUserView
import com.okada.rider.android.ui.login.LoginResult

class RegisterViewModel(private val registerRepository: RegisterRepository) : ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun fetchUserData() {
        registerRepository.fetchEmailAddress {result ->
            result.fold(onSuccess = {user->
                _registerForm.value =
                    RegisterFormState(emailAddress = user.email)

            },onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }
    }
    fun register(firstname: String,
                 lastname: String,
                 biometricId: String) {

        registerRepository
        // can be launched in a separate asynchronous job
       // val result = registerRepository.login(username, password)

       /* if (result is Result.Success) {
            _loginResult.value =
                RegisterResult(success = RegisteredUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = RegisterResult(error = R.string.login_failed)
        }*/
    }

    fun dataChanged(firstname: String, surname: String) {
        if (!isStringValid(firstname)) {
            _registerForm.value = RegisterFormState(firstNameError = R.string.invalid_string)
        } else if (!isPasswordValid(surname)) {
            _registerForm.value = RegisterFormState(surnameError = R.string.invalid_string)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder string validation check
    private fun isStringValid(str: String): Boolean {
        if (str.isNotBlank()) {
            if (!str.matches(Regex(".*\\d.*"))) {
                // has no number characters
                return true
            }
        }
        return false
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}