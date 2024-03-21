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
        registerRepository.fetchEmailAddress { result ->
            result.fold(onSuccess = { user ->
                _registerForm.value =
                    RegisterFormState(emailAddress = user.email)
                registerRepository.checkProfileExists()

            }, onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }
    }

    fun register(
        firstname: String,
        lastname: String,
        biometricId: String
    ) {
        registerRepository.createUserInfo(firstname, lastname, biometricId) { result ->
            result.fold(onSuccess = {
                _registerResult.value =
                    RegisterResult(stringResource = R.string.profile_created)

            }, onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }

    }

    fun dataChanged(firstname: String, surname: String) {
        var formState = RegisterFormState()
        var valid = true
        if (!isStringValid(firstname)) {
            formState.firstNameError = R.string.invalid_string
            valid = false
        }
        if (!isStringValid(surname)) {
            formState.surnameError = R.string.invalid_string
            valid = false
        }
        if (firstname.isNotBlank() &&
            surname.isNotBlank() && valid
        ) {
            formState.isDataValid = true
        }
        _registerForm.value = formState
    }


    // A placeholder string validation check
    private fun isStringValid(str: String): Boolean {
        // has no number characters
        return !str.matches(Regex(".*\\d.*"))
        // contains number characters
    }
}