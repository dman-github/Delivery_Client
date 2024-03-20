package com.okada.rider.android.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val navigateToRegister: Boolean? = false,
    val navigateToHome: Boolean? = false,
    val errorMsg: String? = null
)