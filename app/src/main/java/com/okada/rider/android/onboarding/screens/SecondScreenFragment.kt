package com.okada.rider.android.onboarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.okada.rider.android.R


/**
 * A simple [Fragment] subclass.
 * Use the [SecondScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SecondScreenFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_second_screen, container, false)
        val next = view.findViewById<TextView>(R.id.second_screen_next)
        next.setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_homeFragment)
        }
        return view
    }

}