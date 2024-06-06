import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.okada.rider.android.data.AccountUsecase
import com.okada.rider.android.data.DirectionsUsecase
import com.okada.rider.android.data.DriverRequestUsecase
import com.okada.rider.android.data.LocationUsecase
import com.okada.rider.android.data.ProfileUsecase
import com.okada.rider.android.services.AccountServiceImpl
import com.okada.rider.android.services.DataServiceImpl
import com.okada.rider.android.services.DriverRequestServiceImpl
import com.okada.rider.android.services.LocationServiceImpl
import com.okada.rider.android.ui.requestDriver.RequestDriverViewModel

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class RequestDriverViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RequestDriverViewModel::class.java)) {
            return RequestDriverViewModel(
                directionsUsecase = DirectionsUsecase(),
                driverRequestUsecase = DriverRequestUsecase(
                    driverRequestService = DriverRequestServiceImpl(),
                    dataService = DataServiceImpl()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}