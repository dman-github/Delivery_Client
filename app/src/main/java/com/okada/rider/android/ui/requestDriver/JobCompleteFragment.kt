package com.okada.rider.android.ui.requestDriver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.intl.Locale
import androidx.fragment.app.DialogFragment
import com.okada.rider.android.R
import com.okada.rider.android.data.model.SelectedPlaceEvent
import com.okada.rider.android.databinding.FragmentJobCompleteBinding
import com.okada.rider.android.databinding.FragmentRequestDriverBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JobCompleteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JobCompleteFragment : DialogFragment() {
    private var _binding: FragmentJobCompleteBinding? = null
    private var selectedPlaceEvent: SelectedPlaceEvent? = null
    private lateinit var textOriginAddress: TextView
    private lateinit var textDestAddress: TextView
    private lateinit var textTotalFare: TextView
    private lateinit var textBaseFare: TextView
    private lateinit var textOther: TextView
    private lateinit var textDistance: TextView
    private lateinit var textDuration: TextView
    private lateinit var textTime: TextView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJobCompleteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        init()
        return root
    }

    fun init() {
        textOriginAddress = binding.textOrigin
        textDestAddress = binding.textDestination
        textBaseFare = binding.textFare
        textTotalFare = binding.textTotal
        textOther = binding.textOtherCharges
        textDistance = binding.textDistance
        textDuration = binding.textDuration
        textTime = binding.textDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }


    override fun onStop() {
        super.onStop()

        if (EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvent::class.java))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvent::class.java)

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(event: SelectedPlaceEvent) {
        selectedPlaceEvent = event
        initView()
    }

    fun initView() {
        if (selectedPlaceEvent != null) {
            textOriginAddress.text = selectedPlaceEvent!!.originAddress
            textDestAddress.text = selectedPlaceEvent!!.destAddress
            textBaseFare.text = selectedPlaceEvent!!.priceText
            textDuration.text = selectedPlaceEvent!!.durationText
            textDistance.text = selectedPlaceEvent!!.distanceText
            textOther.text = ""
            textTotalFare.text = selectedPlaceEvent!!.priceText
            val formatter = SimpleDateFormat("yyyy-MM-dd   HH:mm", java.util.Locale.ENGLISH)
            textTime.text = formatter.format(Date())
        }
    }
}