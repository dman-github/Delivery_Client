package com.okada.rider.android.ui.splash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.okada.rider.android.R
import com.okada.rider.android.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    private lateinit var splashViewModel: SplashViewModel
    private var _binding: FragmentSplashBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splashViewModel = ViewModelProvider(this, SplashViewModelFactory())
            .get(SplashViewModel::class.java)

        val loadingProgressBar = binding.loading

        splashViewModel.liveDataMerger.observe(viewLifecycleOwner,
            Observer { signupResult ->
                signupResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                signupResult.errorMsg?.let {
                    showLoginFailed(it)
                }
                signupResult.navigateToRegister?.let {
                    if (it) {
                        navigateToRegisterScreen()
                    }
                }
                signupResult.navigateToHome?.let {
                    if (it) {
                        navigateToHomeScreen()
                    }
                }
                signupResult.navigateToLogin?.let {
                    if (it) {
                        navigateToLoginScreen()
                    }
                }
                signupResult.navigateToOnBoarding?.let {
                    if (it) {
                        navigateToOnboarding()
                    }
                }
            })
    }


    override fun onResume() {
        super.onResume()
        Log.i("App_Info", "SplashFragment onResume")
        if (onBoardingFinishedCheck()) {
            splashViewModel.startSplashTimer()
        } else {
            navigateToOnboarding()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("App_Info", "SplashFragment onPause")
        splashViewModel.removeLiveDataSources()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        splashViewModel.removeLiveDataSources()
    }

    private fun navigateToRegisterScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
    }

    private fun navigateToLoginScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }

    private fun navigateToHomeScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_mainActivity)
    }

    private fun navigateToOnboarding() {
        findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
    }

    private fun onBoardingFinishedCheck(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("finished", false)
    }
    private fun showLoginFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

}