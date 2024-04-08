package com.okada.rider.android.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okada.rider.android.data.AccountUsecase

class AccountViewModel(private val accountUsecase: AccountUsecase) : ViewModel() {

    fun logoutUser() {
        accountUsecase.logout{}
    }
}