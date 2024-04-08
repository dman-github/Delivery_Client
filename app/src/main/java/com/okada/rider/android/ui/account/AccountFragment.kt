package com.okada.rider.android.ui.account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.okada.rider.android.Common
import com.okada.rider.android.R
import com.okada.rider.android.data.model.UserInfo
import com.okada.rider.android.databinding.FragmentAccountBinding
import com.okada.rider.android.ui.login.LoginViewModel

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var navView: NavigationView
    private lateinit var imgAvatar: ImageView
    private lateinit var textName: TextView
    private lateinit var textEmail: TextView
    private lateinit var textStar: TextView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountViewModel =
            ViewModelProvider(this, AccountViewModelFactory()).get(AccountViewModel::class.java)

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navView = binding.navView
        init()
        return root
    }

    private fun init() {
        textName = binding.txtName
        textEmail = binding.txtEmail
        textStar = binding.txtStar
        imgAvatar = binding.imgAvatar
        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_exit) {
                var builder = AlertDialog.Builder(requireActivity())
                builder.setTitle(R.string.menu_logout)
                    .setMessage(R.string.sign_out_msg)
                    .setNegativeButton(R.string.cancel_string) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .setPositiveButton(R.string.menu_logout) { dialogInterface, _ ->
                        accountViewModel.logoutUser()
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
        updateGuiWithUserInfo()
    }

    private fun updateGuiWithUserInfo() {
        Common.currentUser?.let { user ->
            textName.text = Common.buildFullname()
            textEmail.text = user.email
            textStar.text = StringBuilder().append(user.rating)
            if (!user.avatar.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.avatar)
                    .into(imgAvatar)
            } else {
                Glide.with(this)
                    .load(R.drawable.okada_logo)
                    .into(imgAvatar)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}