package com.okada.rider.android.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.okada.rider.android.R
import com.okada.rider.android.onboarding.screens.firstScreenFragment
import com.okada.rider.android.onboarding.screens.SecondScreenFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewPagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewPagerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
            firstScreenFragment(),
            SecondScreenFragment()
        )
        val adaptor = ViewPagerAdaptor(fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle)

        view.findViewById<ViewPager2>(R.id.view_pager).adapter = adaptor

        return view
    }


}