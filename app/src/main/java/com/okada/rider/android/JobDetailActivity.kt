package com.okada.rider.android

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.SupportMapFragment
import com.okada.rider.android.databinding.ActivityJobDetailBinding
import com.okada.rider.android.databinding.ActivityRequestDriverBinding

class JobDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }


    private fun init() {

    }
}