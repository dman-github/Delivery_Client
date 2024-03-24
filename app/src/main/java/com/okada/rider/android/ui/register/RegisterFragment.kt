package com.okada.rider.android.ui.register

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.okada.rider.android.databinding.FragmentRegisterBinding

import com.okada.rider.android.R

class RegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory())
            .get(RegisterViewModel::class.java)

        val firstnameEditText = binding.editFirstName
        val firstnameLayout = binding.layoutFirstname
        val lastNameEditText = binding.editLastName
        val lastnameLayout = binding.layoutLastname
        val emailEditText = binding.emailAddress
        val biomEditText = binding.idNumber
        val profileButton = binding.createProfile
        val loadingProgressBar = binding.loading
        registerViewModel.fetchUserData()
        registerViewModel.registerFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                profileButton.isEnabled = loginFormState.isDataValid
                loginFormState.firstNameError?.let {
                    firstnameLayout.error = getString(it)
                }
                loginFormState.surnameError?.let {
                    lastnameLayout.error = getString(it)
                }
                if (loginFormState.emailAddress.isNotEmpty()) {
                    emailEditText.setText(loginFormState.emailAddress)
                }
            })

        registerViewModel.registerResult.observe(viewLifecycleOwner,
            Observer { registerResult ->
                registerResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                registerResult.errorMsg?.let {
                    showApiFailed(it)
                }
                registerResult.navigateToHome?.let {
                    navigateToHomeScreen()
                }
                registerResult.stringResource?.let{
                    showApiMessage(it)
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
                registerViewModel.dataChanged(
                    firstnameEditText.text.toString(),
                    lastNameEditText.text.toString()
                )
            }
        }

        firstnameEditText.addTextChangedListener(afterTextChangedListener)
        lastNameEditText.addTextChangedListener(afterTextChangedListener)

        profileButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            registerViewModel.register(
                firstnameEditText.text.toString(),
                lastNameEditText.text.toString(),
                biomEditText.text.toString()
            )
        }
    }

    private fun navigateToHomeScreen () {
        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
    }

    private fun updateUiWithUser(model: RegisteredUserView) {
        val welcome = getString(R.string.welcome) + model.uid
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showApiFailed(errorString: String) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun showApiMessage(@StringRes messageString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, messageString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}