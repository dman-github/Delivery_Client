package com.okada.rider.android.ui.splash


data class SplashResult(
    var navigateToOnBoarding: Boolean? = false,
    var navigateToLogin: Boolean? = false,
    var navigateToRegister: Boolean? = false,
    var navigateToHome: Boolean? = false,
    val errorMsg: String? = null,
    val stringResource: Int? = null
)