package com.okada.rider.android.ui.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.R
import com.okada.rider.android.data.SignupRepository
import com.okada.rider.android.ui.login.LoggedInUserView
import com.okada.rider.android.ui.login.LoginFormState
import com.okada.rider.android.ui.login.LoginResult
import com.okada.rider.android.ui.register.RegisterFormState
import com.okada.rider.android.ui.register.RegisterResult

class SignupViewModel(private val signupRepository: SignupRepository) : ViewModel() {

    private val _signupForm = MutableLiveData<SignupFormState>()
    val signupFormState: LiveData<SignupFormState> = _signupForm

    private val _signupResult = MutableLiveData<SignupResult>()
    val signupResult: LiveData<SignupResult> = _signupResult

    fun signup(username: String, password: String) {
        signupRepository.createUser(username, password) {result ->
            result.fold(onSuccess = {user->
                _signupResult.value =
                    SignupResult(success = LoggedInUserView(displayName = user.userId))

            },onFailure = {
                _signupResult.value = SignupResult(errorMsg = it.message)
            })
        }
    }

    fun dataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _signupForm.value = SignupFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _signupForm.value = SignupFormState(passwordError = R.string.invalid_password)
        } else {
            _signupForm.value = SignupFormState(isDataValid = true)
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
