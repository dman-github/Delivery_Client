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
                /*_registerForm.value =
                    RegisterFormState(emailAddress = user.email)*/
                registerRepository.checkProfileExists()

            },onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }
    }
    fun register(firstname: String,
                 lastname: String,
                 biometricId: String,
                 username: String,
                 password: String) {
        registerRepository.createUserAuthentication(username, password){result ->
            result.fold(onSuccess = {
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
            },onFailure = {
                _registerResult.value = RegisterResult(errorMsg = it.message)
            })
        }
    }

    fun dataChanged(firstname: String, surname: String, password: String, retryPassword:String) {
        if (!isStringValid(firstname)) {
            _registerForm.value = RegisterFormState(firstNameError = R.string.invalid_string)
        }
        if (!isStringValid(surname)) {
            _registerForm.value = RegisterFormState(surnameError = R.string.invalid_string)
        }

        if (password.isNotBlank() && !isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else {
            //Password is valid, now check the retryPassword
            if (retryPassword.isNotBlank() && !arePasswordsIdentical(password,retryPassword)) {
                    _registerForm.value =
                        RegisterFormState(passwordMatchingError = R.string.invalid_matching_password)
            } else {
                if (firstname.isNotBlank() &&
                    surname.isNotBlank() &&
                    password.isNotBlank() &&
                    retryPassword.isNotBlank()) {
                    _registerForm.value = RegisterFormState(isDataValid = true)
                } else {
                    _registerForm.value = RegisterFormState(isDataValid = false)
                }
            }
        }
    }

    // A placeholder string validation check
    private fun isStringValid(str: String): Boolean {
        if (str.isNotBlank()) {
            if (!str.matches(Regex(".*\\d.*"))) {
                // has no number characters
                return true
            } else {
                // contains number characters
                return false
            }
        }
        return true
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun arePasswordsIdentical(password: String, retryPassword: String): Boolean {
        return password == retryPassword
    }
}