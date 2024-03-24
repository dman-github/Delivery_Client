package com.okada.rider.android.ui.splash

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.okada.rider.android.R
import com.okada.rider.android.databinding.FragmentSignupBinding
import com.okada.rider.android.databinding.FragmentSplashBinding
import com.okada.rider.android.ui.login.LoggedInUserView

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
        splashViewModel.startSplashTimer()
    }

    private fun navigateToRegisterScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
    }

    private fun navigateToLoginScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
    }

    private fun navigateToHomeScreen() {
        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
    }

    private fun navigateToOnboarding() {
        findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
    }

    private fun showLoginFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}