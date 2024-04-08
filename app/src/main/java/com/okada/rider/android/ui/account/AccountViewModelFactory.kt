package com.okada.rider.android.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.services.AccountServiceImpl
import com.okada.rider.android.services.DataServiceImpl

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class AccountViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(
                accountUsecase = AccountUsecase(
                    accountService = AccountServiceImpl()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}