package com.okada.rider.android.ui.signup

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
import com.okada.rider.android.ui.login.LoggedInUserView
import com.okada.rider.android.ui.login.LoginViewModel
import com.okada.rider.android.ui.login.LoginViewModelFactory

class SignupFragment : Fragment() {
    private lateinit var signupViewModel: SignupViewModel
    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signupViewModel = ViewModelProvider(this, SignupViewModelFactory())
            .get(SignupViewModel::class.java)

        val usernameEditText = binding.emailInput
        val usernameLayout = binding.layoutEmail
        val passwordEditText = binding.passInput
        val passwordLayout = binding.layoutPassword
        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val textViewRegister = binding.textViewRegister

        signupViewModel.signupFormState.observe(viewLifecycleOwner,
            Observer { signupFormState ->
                if (signupFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = signupFormState.isDataValid
                signupFormState.usernameError?.let {
                    usernameLayout.error = getString(it)
                } ?: run {
                    usernameLayout.error = null
                }
                signupFormState.passwordError?.let {
                    passwordLayout.error = getString(it)
                } ?: run {
                    passwordLayout.error = null
                }
            })

        signupViewModel.signupResult.observe(viewLifecycleOwner,
            Observer { signupResult ->
                signupResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                signupResult.errorMsg?.let {
                    showLoginFailed(it)
                }
                signupResult.success?.let {
                    updateUiWithUser(it)
                }
                signupResult.navigateToRegister?.let {
                    if (it) {
                        navigateToRegisterScreen()
                    }
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                signupViewModel.dataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loadingProgressBar.visibility = View.VISIBLE
                signupViewModel.signup(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            signupViewModel.signup(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        textViewRegister.setOnClickListener {
        }
    }

    private fun navigateToRegisterScreen() {
        findNavController().navigate(R.id.action_signupFragment_to_registerFragment)
    }


    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
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