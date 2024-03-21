package com.okada.rider.android.ui.register

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    var firstNameError: Int? = null,
    var surnameError: Int? = null,
    var emailError: Int? = null,
    var isDataValid: Boolean = false,
    var emailAddress: String = ""
)