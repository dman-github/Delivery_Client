package com.okada.rider.android.ui.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.R
import com.okada.rider.android.data.SignupUsecase

class SignupViewModel(private val signupRepository: SignupUsecase) : ViewModel() {

    private val _signupForm = MutableLiveData<SignupFormState>()
    val signupFormState: LiveData<SignupFormState> = _signupForm

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

    fun dataChanged(username: String, password: String) {
        var formState = SignupFormState()
        var valid = true
        if (username.isNotBlank() && !isUserNameValid(username)) {
            formState.usernameError = R.string.invalid_username
            valid = false
           // _signupForm.value = SignupFormState(usernameError = R.string.invalid_username)
        }
        if (password.isNotBlank() && !isPasswordValid(password)) {
            //_signupForm.value = SignupFormState(passwordError = R.string.invalid_password)
            formState.passwordError = R.string.invalid_password
            valid = false
        }

        if (valid && username.isNotBlank() && password.isNotBlank()) {
            formState.isDataValid = true
        }
        _signupForm.value = formState
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else return false
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
