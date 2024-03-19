package com.okada.rider.android.ui.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val firstNameError: Int? = null,
    val surnameError: Int? = null,
    val passwordError: Int? = null,
    val passwordMatchingError: Int? = null,
    val emailError: Int? = null,
    val isDataValid: Boolean = false,
    val emailAddress: String = ""
)