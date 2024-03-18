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
                registerRepository.checkProfileExists()

            },onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }
    }
    fun register(firstname: String,
                 lastname: String,
                 biometricId: String) {

        if (!registerRepository.isProfileExists) {
            // Profile does not exist in DB so we can create one
            registerRepository.createUserInfo(firstname,lastname,biometricId) {result ->
                result.fold(onSuccess = {
                    _registerResult.value =
                        RegisterResult(stringResource = R.string.profile_created)

                },onFailure = {
                    _registerResult.value = RegisterResult(errorMsg = it.message)
                })
            }

        } else {
            _registerResult.value = RegisterResult(stringResource = R.string.user_profile_exists)
        }
    }

    fun dataChanged(firstname: String, surname: String) {
        if (!isStringValid(firstname)) {
            _registerForm.value = RegisterFormState(firstNameError = R.string.invalid_string)
        } else if (!isStringValid(surname)) {
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