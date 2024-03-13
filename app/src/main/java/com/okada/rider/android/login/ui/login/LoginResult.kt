package com.okada.rider.android.login.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val errorMsg: String? = null
)