package com.okada.rider.android.ui.register

/**
 * Authentication result : success (user details) or error message.
 */
data class RegisterResult(
    val navigateToHome: Boolean? = null,
    val errorMsg: String? = null,
    val stringResource: Int? = null
)