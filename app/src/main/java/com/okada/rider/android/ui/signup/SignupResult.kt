package com.okada.rider.android.ui.signup

import com.okada.rider.android.ui.login.LoggedInUserView


data class SignupResult(
    val success: LoggedInUserView? = null,
    val errorMsg: String? = null,
    val stringResource: Int? = null
)