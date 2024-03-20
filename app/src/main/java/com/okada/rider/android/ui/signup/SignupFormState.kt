package com.okada.rider.android.ui.signup


data class SignupFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)