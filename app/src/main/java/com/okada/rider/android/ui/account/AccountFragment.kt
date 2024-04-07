package com.okada.rider.android.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.okada.rider.android.R
import com.okada.rider.android.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private lateinit var navView: NavigationView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accountViewModel =
            ViewModelProvider(this).get(AccountViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navView = binding.navView
        init()
        return root
    }

    private fun init() {
        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_exit) {
                var builder = AlertDialog.Builder(requireActivity())
                builder.setTitle(R.string.menu_logout)
                    .setMessage(R.string.sign_out_msg)
                    .setNegativeButton(R.string.cancel_string) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton(R.string.menu_logout) { dialogInterface, _ ->

                        FirebaseAuth.getInstance().signOut()
                        /*val intent =
                            Intent(this@DriverHomeActivity, SplashScreenActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)*/
                        requireActivity().finish()
                    }.setCancelable(false)

                val dialog = builder.create()
                dialog.setOnShowListener {
                    val priColor = MaterialColors.getColor(requireActivity(), android.R.attr.colorPrimary, Color.WHITE);
                    val accColor = MaterialColors.getColor(requireActivity(), android.R.attr.colorAccent, Color.WHITE);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(priColor)
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(accColor)
                }

                dialog.show()
            }
            return@setNavigationItemSelectedListener true
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}