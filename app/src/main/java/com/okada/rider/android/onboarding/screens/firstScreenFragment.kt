package com.okada.rider.android.onboarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.okada.rider.android.R

/**
 * A simple [Fragment] subclass.
 * Use the [firstScreenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class firstScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_first_screen, container, false)
        activity?.let { activity ->
            val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
            val next = view.findViewById<TextView>(R.id.first_screen_next)
            next.setOnClickListener{
                viewPager?.currentItem  = 1
            }
        }
        return view
    }

}